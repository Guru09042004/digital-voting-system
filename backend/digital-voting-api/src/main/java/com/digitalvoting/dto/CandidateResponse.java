package com.digitalvoting.dto;

import java.time.OffsetDateTime;

public record CandidateResponse(
        Long id,
        Long electionId,
        String candidateCode,
        String candidateName,
        String partyName,
        String symbol,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
