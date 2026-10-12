package com.digitalvoting.service;

import com.digitalvoting.dto.CreateElectionRequest;
import com.digitalvoting.dto.ElectionResponse;
import com.digitalvoting.entity.Election;
import com.digitalvoting.repository.ElectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;

    public ElectionService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    public ElectionResponse createElection(CreateElectionRequest request) {

        if (electionRepository.existsByElectionCode(request.getElectionCode())) {
            throw new IllegalArgumentException(
                    "Election code already exists."
            );
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        Election election = new Election();

        election.setElectionCode(request.getElectionCode());
        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        election.setStatus("DRAFT");

        Election savedElection = electionRepository.save(election);

        return toResponse(savedElection);
    }

    public ElectionResponse getElection(String electionCode) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Election not found."
                        )
                );

        return toResponse(election);
    }

    public List<ElectionResponse> getAllElections() {

        return electionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ElectionResponse updateStatus(
            String electionCode,
            String newStatus) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Election not found."
                        )
                );

        String currentStatus = election.getStatus();

        validateTransition(currentStatus, newStatus);

        election.setStatus(newStatus);

        Election updatedElection =
                electionRepository.save(election);

        return toResponse(updatedElection);
    }

    private void validateTransition(
            String currentStatus,
            String newStatus) {

        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid election status: " + newStatus
            );
        }

        boolean validTransition =
                switch (currentStatus) {

                    case "DRAFT" ->
                            newStatus.equals("SCHEDULED");

                    case "SCHEDULED" ->
                            newStatus.equals("LIVE");

                    case "LIVE" ->
                            newStatus.equals("CLOSED");

                    case "CLOSED" ->
                            newStatus.equals("RESULT_DECLARED");

                    case "RESULT_DECLARED" ->
                            false;

                    default ->
                            false;
                };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid election status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }
    }

    private boolean isValidStatus(String status) {

        return status.equals("DRAFT")
                || status.equals("SCHEDULED")
                || status.equals("LIVE")
                || status.equals("CLOSED")
                || status.equals("RESULT_DECLARED");
    }

    private ElectionResponse toResponse(Election election) {

        return new ElectionResponse(
                election.getId(),
                election.getElectionCode(),
                election.getTitle(),
                election.getDescription(),
                election.getStartTime(),
                election.getEndTime(),
                election.getStatus()
        );
    }
}
