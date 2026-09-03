package com.example.urlshortener.web;

import com.example.urlshortener.health.DatabaseReadiness;
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

    public HealthController(DatabaseReadiness databaseReadiness) {
        this.databaseReadiness = databaseReadiness;
    }

    @GetMapping("/live")
    public HealthResponse live() {
        return UP;
    }

    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> ready() {
        return databaseReadiness.isReady()
                ? ResponseEntity.ok(UP)
                : ResponseEntity.status(503).body(DOWN);
    }

    public record HealthResponse(String status) {}
}
