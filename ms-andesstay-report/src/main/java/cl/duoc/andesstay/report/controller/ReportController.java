package cl.duoc.andesstay.report.controller;

import cl.duoc.andesstay.report.service.ReportService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public Map<String, Object> kpis(@RequestParam(defaultValue = "last24h") String range) {
        return service.kpis(range);
    }

    @GetMapping("/top-units")
    public List<Map<String, Object>> topUnits(@RequestParam(defaultValue = "last7d") String range) {
        return service.topUnits(range);
    }
}
