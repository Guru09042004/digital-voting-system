package com.digitalvoting.dto;

import jakarta.validation.constraints.NotBlank;

public class CastVoteRequest {

    @NotBlank
    private String electionCode;

    @NotBlank
    private String candidateCode;

    public String getElectionCode() {
        return electionCode;
    }

    public void setElectionCode(String electionCode) {
        this.electionCode = electionCode;
    }

    public String getCandidateCode() {
        return candidateCode;
    }

    public void setCandidateCode(String candidateCode) {
        this.candidateCode = candidateCode;
    }
}
