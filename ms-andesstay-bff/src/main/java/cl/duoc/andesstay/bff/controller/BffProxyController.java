package cl.duoc.andesstay.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;

@RestController
public class BffProxyController {
    private static final Set<String> FORWARDED_RESPONSE_HEADERS =
            Set.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.CACHE_CONTROL, HttpHeaders.LOCATION);

    private final RestClient restClient = RestClient.create();
    private final String reservationsUrl;
    private final String catalogUrl;
    private final String auditUrl;
    private final String reportUrl;

    public BffProxyController(
            @Value("${services.reservations.url:http://reservations:8081}") String reservationsUrl,
            @Value("${services.catalog.url:http://catalog:8083}") String catalogUrl,
            @Value("${services.audit.url:http://audit:8084}") String auditUrl,
            @Value("${services.report.url:http://report:8085}") String reportUrl) {
        this.reservationsUrl = reservationsUrl;
        this.catalogUrl = catalogUrl;
        this.auditUrl = auditUrl;
        this.reportUrl = reportUrl;
    }

    @RequestMapping("/api/reservations/**")
    public ResponseEntity<byte[]> reservations(HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) byte[] body) throws IOException {
        return forward(reservationsUrl, request, authorization, body);
    }

    @RequestMapping("/api/catalog/**")
    public ResponseEntity<byte[]> catalog(HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) byte[] body) throws IOException {
        return forward(catalogUrl, request, authorization, body);
    }

    @RequestMapping("/api/audit/**")
    public ResponseEntity<byte[]> audit(HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) byte[] body) throws IOException {
        return forward(auditUrl, request, authorization, body);
    }

    @RequestMapping("/api/report/**")
    public ResponseEntity<byte[]> report(HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) byte[] body) throws IOException {
        return forward(reportUrl, request, authorization, body);
    }

    private ResponseEntity<byte[]> forward(String baseUrl, HttpServletRequest request,
            String authorization, byte[] body) throws IOException {
        String query = request.getQueryString();
        URI target = URI.create(baseUrl + request.getRequestURI() + (query == null ? "" : "?" + query));
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        RequestBodySpec outgoing = restClient.method(method).uri(target).headers(headers -> {
                    if (authorization != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                    String contentType = request.getContentType();
                    if (contentType != null) {
                        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
                    }
                    String accept = request.getHeader(HttpHeaders.ACCEPT);
                    if (accept != null) {
                        headers.set(HttpHeaders.ACCEPT, accept);
                    }
                });
        if (body != null && body.length > 0) {
            outgoing.body(body);
        }
        return outgoing.exchange((clientRequest, clientResponse) -> {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    clientResponse.getHeaders().forEach((name, values) -> {
                        if (FORWARDED_RESPONSE_HEADERS.stream().anyMatch(header -> header.equalsIgnoreCase(name))) {
                            responseHeaders.put(name, values);
                        }
                    });
                    return ResponseEntity.status(clientResponse.getStatusCode())
                            .headers(responseHeaders)
                            .body(StreamUtils.copyToByteArray(clientResponse.getBody()));
                });
    }
}
