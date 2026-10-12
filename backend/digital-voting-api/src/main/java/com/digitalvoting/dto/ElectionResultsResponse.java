package com.digitalvoting.dto;

import java.util.List;

public record ElectionResultsResponse(
        String electionCode,
        String electionTitle,
        String status,
        long totalVotes,
        List<CandidateResult> candidates
) {
    public record CandidateResult(
            String candidateCode,
            String candidateName,
            String partyName,
            String symbol,
            long votes
    ) {}
}
