package com.digitalvoting.repository;

import com.digitalvoting.entity.Election;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, Long> {

    boolean existsByElectionCode(String electionCode);

    Optional<Election> findByElectionCode(String electionCode);
}
