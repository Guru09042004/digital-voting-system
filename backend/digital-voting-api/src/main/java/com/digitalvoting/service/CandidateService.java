package com.digitalvoting.service;

import com.digitalvoting.dto.CandidateResponse;
import com.digitalvoting.dto.CreateCandidateRequest;
import com.digitalvoting.dto.UpdateCandidateRequest;
import com.digitalvoting.entity.Candidate;
import com.digitalvoting.entity.Election;
import com.digitalvoting.repository.CandidateRepository;
import com.digitalvoting.repository.ElectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;

    public CandidateService(
            CandidateRepository candidateRepository,
            ElectionRepository electionRepository) {

        this.candidateRepository = candidateRepository;
        this.electionRepository = electionRepository;
    }

    public CandidateResponse createCandidate(
            String electionCode,
            CreateCandidateRequest request) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Election not found."));

        validateCandidateManagementAllowed(election);

        if (candidateRepository.existsByCandidateCode(
                request.getCandidateCode())) {

            throw new IllegalArgumentException(
                    "Candidate code already exists.");
        }

        Candidate candidate = new Candidate();

        candidate.setElectionId(election.getId());
        candidate.setCandidateCode(request.getCandidateCode());
        candidate.setCandidateName(request.getCandidateName());
        candidate.setPartyName(request.getPartyName());
        candidate.setSymbol(request.getSymbol());
        candidate.setStatus("ACTIVE");

        Candidate savedCandidate =
                candidateRepository.save(candidate);

        return toResponse(savedCandidate);
    }

    public List<CandidateResponse> getCandidates(
            String electionCode) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Election not found."));

        return candidateRepository
                .findByElectionIdAndStatus(election.getId(), "ACTIVE")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CandidateResponse updateCandidate(
            String electionCode,
            String candidateCode,
            UpdateCandidateRequest request) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Election not found."));

        Candidate candidate = candidateRepository
                .findByCandidateCode(candidateCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Candidate not found."));

        if (!candidate.getElectionId().equals(election.getId())) {
            throw new IllegalArgumentException(
                    "Candidate does not belong to this election.");
        }

        validateCandidateManagementAllowed(election);

        String status = request.getStatus().toUpperCase();

        if (!status.equals("ACTIVE") &&
            !status.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Invalid candidate status. Use ACTIVE or INACTIVE.");
        }

        candidate.setCandidateName(request.getCandidateName());
        candidate.setPartyName(request.getPartyName());
        candidate.setSymbol(request.getSymbol());
        candidate.setStatus(status);

        Candidate updatedCandidate =
                candidateRepository.save(candidate);

        return toResponse(updatedCandidate);
    }

    private void validateCandidateManagementAllowed(
            Election election) {

        String status = election.getStatus();

        if (!status.equals("DRAFT") &&
            !status.equals("SCHEDULED")) {

            throw new IllegalArgumentException(
                    "Candidate management is not allowed when election status is "
                            + status + ".");
        }
    }

    private CandidateResponse toResponse(
            Candidate candidate) {

        return new CandidateResponse(
                candidate.getId(),
                candidate.getElectionId(),
                candidate.getCandidateCode(),
                candidate.getCandidateName(),
                candidate.getPartyName(),
                candidate.getSymbol(),
                candidate.getStatus(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }
}
