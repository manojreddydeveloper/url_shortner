package com.example.urlshortener.web;

import com.example.urlshortener.health.DatabaseReadiness;
import com.example.urlshortener.observability.OperationalMetrics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    private static final HealthResponse UP = new HealthResponse("UP");
    private static final HealthResponse DOWN = new HealthResponse("DOWN");

    private final DatabaseReadiness databaseReadiness;
    private final OperationalMetrics metrics;

    @Autowired
    public HealthController(DatabaseReadiness databaseReadiness, ObjectProvider<OperationalMetrics> metrics) {
        this(databaseReadiness, metrics.getIfAvailable());
    }

    public HealthController(DatabaseReadiness databaseReadiness) {
        this(databaseReadiness, (OperationalMetrics) null);
    }
    private HealthController(DatabaseReadiness databaseReadiness, OperationalMetrics metrics) {
        this.databaseReadiness = databaseReadiness; this.metrics = metrics;
    }

    @GetMapping("/live")
    public HealthResponse live() {
        return UP;
    }

    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> ready() {
        boolean ready = databaseReadiness.isReady();
        if (metrics != null) metrics.readiness(ready);
        return ready
                ? ResponseEntity.ok(UP)
                : ResponseEntity.status(503).body(DOWN);
    }

    public record HealthResponse(String status) {}
}
