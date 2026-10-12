package com.digitalvoting.dto;

import java.time.OffsetDateTime;

public record ElectionResponse(
        Long id,
        String electionCode,
        String title,
        String description,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status
) {
}
