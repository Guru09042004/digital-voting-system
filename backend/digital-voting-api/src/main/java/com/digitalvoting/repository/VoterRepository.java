package com.digitalvoting.repository;

import com.digitalvoting.entity.Voter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoterRepository extends JpaRepository<Voter, Long> {

    boolean existsByVoterId(String voterId);

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    Optional<Voter> findByVoterId(String voterId);
}
