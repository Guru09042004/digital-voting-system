package com.digitalvoting.repository;

import com.digitalvoting.entity.VoterEligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoterEligibilityRepository
        extends JpaRepository<VoterEligibility, Long> {

    Optional<VoterEligibility> findByElectionIdAndVoterId(
            Long electionId,
            Long voterId
    );

    List<VoterEligibility> findByElectionId(Long electionId);

    List<VoterEligibility> findByElectionIdAndStatus(
            Long electionId,
            String status
    );

    boolean existsByElectionIdAndVoterId(
            Long electionId,
            Long voterId
    );
}
