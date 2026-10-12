package com.digitalvoting.dto;

public record LoginResponse(
        String message,
        String voterId,
        String status,
        String token
) {}
