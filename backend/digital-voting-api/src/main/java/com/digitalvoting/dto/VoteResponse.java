package com.digitalvoting.dto;

import java.time.OffsetDateTime;

public record VoteResponse(
        String message,
        String electionCode,
        String candidateCode,
        String receiptId,
        OffsetDateTime votedAt
) {}
