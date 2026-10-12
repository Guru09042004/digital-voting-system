package com.digitalvoting.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "votes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "votes_unique_voter_election",
            columnNames = {"election_id", "voter_id"}
        )
    }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "receipt_id", nullable = false, unique = true, length = 100)
    private String receiptId;

    @Column(name = "voted_at", nullable = false)
    private OffsetDateTime votedAt;

    @PrePersist
    protected void onCreate() {
        votedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getElectionId() {
        return electionId;
    }

    public void setElectionId(Long electionId) {
        this.electionId = electionId;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public OffsetDateTime getVotedAt() {
        return votedAt;
    }
}
