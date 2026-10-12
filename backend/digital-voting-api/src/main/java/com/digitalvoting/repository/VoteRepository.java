package com.digitalvoting.repository;

import com.digitalvoting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByElectionIdAndVoterId(
            Long electionId,
            Long voterId
    );

    Optional<Vote> findByElectionIdAndVoterId(
            Long electionId,
            Long voterId
    );

    Optional<Vote> findByReceiptId(
            String receiptId
    );

    List<Vote> findByElectionId(
            Long electionId
    );

    long countByElectionIdAndCandidateId(
            Long electionId,
            Long candidateId
    );
}
