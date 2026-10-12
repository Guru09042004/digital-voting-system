package com.digitalvoting.service;

import com.digitalvoting.dto.EligibilityRequest;
import com.digitalvoting.entity.Election;
import com.digitalvoting.entity.Voter;
import com.digitalvoting.entity.VoterEligibility;
import com.digitalvoting.repository.ElectionRepository;
import com.digitalvoting.repository.VoterEligibilityRepository;
import com.digitalvoting.repository.VoterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EligibilityService {

    private final ElectionRepository electionRepository;
    private final VoterRepository voterRepository;
    private final VoterEligibilityRepository voterEligibilityRepository;

    public EligibilityService(
            ElectionRepository electionRepository,
            VoterRepository voterRepository,
            VoterEligibilityRepository voterEligibilityRepository) {

        this.electionRepository = electionRepository;
        this.voterRepository = voterRepository;
        this.voterEligibilityRepository = voterEligibilityRepository;
    }

    @Transactional
    public VoterEligibility updateEligibility(
            String electionCode,
            EligibilityRequest request) {

        Election election = electionRepository
                .findByElectionCode(electionCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Election not found."));

        Voter voter = voterRepository
                .findByVoterId(request.getVoterId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Voter not found."));

        String status = request.getStatus().toUpperCase();

        if (!status.equals("ELIGIBLE")
                && !status.equals("INELIGIBLE")) {

            throw new IllegalArgumentException(
                    "Eligibility status must be ELIGIBLE or INELIGIBLE.");
        }

        VoterEligibility eligibility =
                voterEligibilityRepository
                        .findByElectionIdAndVoterId(
                                election.getId(),
                                voter.getId())
                        .orElseGet(VoterEligibility::new);

        eligibility.setElectionId(election.getId());
        eligibility.setVoterId(voter.getId());
        eligibility.setStatus(status);

        return voterEligibilityRepository.save(eligibility);
    }
}
