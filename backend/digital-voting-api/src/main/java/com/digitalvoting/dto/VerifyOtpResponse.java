package com.digitalvoting.dto;

public record VerifyOtpResponse(
        String message,
        String voterId,
        String status
) {}
