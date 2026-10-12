package com.digitalvoting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {

        return Map.of(
                "status", "UP",
                "application", "Digital Voting System",
                "backend", "Spring Boot",
                "language", "Java"
        );
    }
}
