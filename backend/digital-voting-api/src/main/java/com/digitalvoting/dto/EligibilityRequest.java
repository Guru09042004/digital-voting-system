package com.digitalvoting.dto;

import jakarta.validation.constraints.NotBlank;

public class EligibilityRequest {

    @NotBlank
    private String voterId;

    @NotBlank
    private String status;

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
