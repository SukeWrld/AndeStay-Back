package cl.duoc.andesstay.audit.controller;

import cl.duoc.andesstay.audit.model.AuditEvent;
import cl.duoc.andesstay.audit.service.AuditTimelineService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditTimelineService service;

    public AuditController(AuditTimelineService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditEvent> search(
            @RequestParam(required = false) String reservaId,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.search(reservaId, actor, eventType, from, to);
    }
}
