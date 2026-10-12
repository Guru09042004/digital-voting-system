package com.digitalvoting.controller;

import com.digitalvoting.dto.ElectionResultsResponse;
import com.digitalvoting.entity.Candidate;
import com.digitalvoting.entity.Election;
import com.digitalvoting.repository.CandidateRepository;
import com.digitalvoting.repository.ElectionRepository;
import com.digitalvoting.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;

    public AdminController(
            ElectionRepository electionRepository,
            CandidateRepository candidateRepository,
            VoteRepository voteRepository) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
    }

    @GetMapping("/api/admin/dashboard")
    public Map<String, String> dashboard(Authentication authentication) {
        return Map.of(
                "message", "ADMIN access successful.",
                "voterId", authentication.getName(),
                "role", authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );
    }

    @GetMapping("/api/admin/elections/{electionCode}/results")
    public ElectionResultsResponse getResults(
            @PathVariable String electionCode) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Election not found."));

        if (!"RESULT_DECLARED".equals(election.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Results have not been declared for this election.");
        }

        List<ElectionResultsResponse.CandidateResult> results =
                candidateRepository.findByElectionId(election.getId())
                        .stream()
                        .map(candidate ->
                                new ElectionResultsResponse.CandidateResult(
                                        candidate.getCandidateCode(),
                                        candidate.getCandidateName(),
                                        candidate.getPartyName(),
                                        candidate.getSymbol(),
                                        voteRepository.countByElectionIdAndCandidateId(
                                                election.getId(),
                                                candidate.getId())
                                ))
                        .toList();

        long totalVotes = results.stream()
                .mapToLong(ElectionResultsResponse.CandidateResult::votes)
                .sum();

        return new ElectionResultsResponse(
                election.getElectionCode(),
                election.getTitle(),
                election.getStatus(),
                totalVotes,
                results
        );
    }
}
