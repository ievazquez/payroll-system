package mx.payroll.system.web.controller;

import mx.payroll.system.processing.monitoring.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitoringService monitoringService;

    public MonitorController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    // GET /api/monitor/123/progress
    @GetMapping("/{periodId}/progress")
    public ResponseEntity<MonitoringService.ProgressReport> getProgress(@PathVariable Long periodId) {
        return ResponseEntity.ok(monitoringService.getProgress(periodId));
    }
}
