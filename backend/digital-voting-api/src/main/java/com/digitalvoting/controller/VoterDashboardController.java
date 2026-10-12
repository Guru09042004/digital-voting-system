package com.digitalvoting.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VoterDashboardController {

    @GetMapping("/api/voter/dashboard")
    public Map<String, String> dashboard(Authentication authentication) {
        return Map.of(
                "message", "VOTER access successful.",
                "voterId", authentication.getName(),
                "role", authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );
    }
}
