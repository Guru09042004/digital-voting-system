package com.digitalvoting.service;

import com.digitalvoting.dto.CastVoteRequest;
import com.digitalvoting.dto.VoteResponse;
import com.digitalvoting.entity.Candidate;
import com.digitalvoting.entity.Election;
import com.digitalvoting.entity.Vote;
import com.digitalvoting.entity.Voter;
import com.digitalvoting.entity.VoterEligibility;
import com.digitalvoting.repository.CandidateRepository;
import com.digitalvoting.repository.ElectionRepository;
import com.digitalvoting.repository.VoteRepository;
import com.digitalvoting.repository.VoterEligibilityRepository;
import com.digitalvoting.repository.VoterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VoteService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoterRepository voterRepository;
    private final VoterEligibilityRepository voterEligibilityRepository;
    private final VoteRepository voteRepository;

    public VoteService(
            ElectionRepository electionRepository,
            CandidateRepository candidateRepository,
            VoterRepository voterRepository,
            VoterEligibilityRepository voterEligibilityRepository,
            VoteRepository voteRepository) {

        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.voterRepository = voterRepository;
        this.voterEligibilityRepository = voterEligibilityRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public VoteResponse castVote(
            String authenticatedVoterId,
            CastVoteRequest request) {

        Voter voter = voterRepository
                .findByVoterId(authenticatedVoterId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Voter not found."));

        Election election = electionRepository
                .findByElectionCode(request.getElectionCode())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Election not found."));

        if (!"LIVE".equals(election.getStatus())) {
            throw new IllegalArgumentException(
                    "Voting is allowed only when election status is LIVE.");
        }

        VoterEligibility eligibility =
                voterEligibilityRepository
                        .findByElectionIdAndVoterId(
                                election.getId(),
                                voter.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Voter is not eligible for this election."));

        if (!"ELIGIBLE".equals(eligibility.getStatus())) {
            throw new IllegalArgumentException(
                    "Voter is not eligible for this election.");
        }

        Candidate candidate = candidateRepository
                .findByCandidateCode(request.getCandidateCode())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate not found."));

        if (!candidate.getElectionId().equals(election.getId())) {
            throw new IllegalArgumentException(
                    "Candidate does not belong to this election.");
        }

        if (!"ACTIVE".equals(candidate.getStatus())) {
            throw new IllegalArgumentException(
                    "Candidate is not active.");
        }

        if (voteRepository.existsByElectionIdAndVoterId(
                election.getId(),
                voter.getId())) {

            throw new IllegalArgumentException(
                    "Voter has already voted in this election.");
        }

        Vote vote = new Vote();
        vote.setElectionId(election.getId());
        vote.setVoterId(voter.getId());
        vote.setCandidateId(candidate.getId());
        vote.setReceiptId(
                "RCPT-" + UUID.randomUUID());

        Vote savedVote = voteRepository.save(vote);

        return new VoteResponse(
                "Vote cast successfully.",
                election.getElectionCode(),
                candidate.getCandidateCode(),
                savedVote.getReceiptId(),
                savedVote.getVotedAt()
        );
    }
}
