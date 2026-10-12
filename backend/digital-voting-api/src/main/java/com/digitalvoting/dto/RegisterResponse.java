package com.digitalvoting.dto;

public record RegisterResponse(
        String message,
        String voterId,
        String status
) {
}
