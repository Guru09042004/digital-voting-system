package com.digitalvoting.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCandidateRequest {

    @NotBlank
    private String candidateCode;

    @NotBlank
    private String candidateName;

    private String partyName;

    private String symbol;

    public String getCandidateCode() {
        return candidateCode;
    }

    public void setCandidateCode(String candidateCode) {
        this.candidateCode = candidateCode;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
