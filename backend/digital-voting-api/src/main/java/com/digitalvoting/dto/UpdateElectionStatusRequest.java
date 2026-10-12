package com.digitalvoting.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateElectionStatusRequest {

    @NotBlank
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
