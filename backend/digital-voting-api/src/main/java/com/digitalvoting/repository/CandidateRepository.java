package com.digitalvoting.repository;

import com.digitalvoting.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByCandidateCode(String candidateCode);

    Optional<Candidate> findByCandidateCode(String candidateCode);

    List<Candidate> findByElectionId(Long electionId);

    List<Candidate> findByElectionIdAndStatus(
            Long electionId,
            String status
    );
}
