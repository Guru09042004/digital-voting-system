package com.digitalvoting.controller;

import com.digitalvoting.dto.CandidateResponse;
import com.digitalvoting.dto.CreateCandidateRequest;
import com.digitalvoting.dto.UpdateCandidateRequest;
import com.digitalvoting.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/elections/{electionCode}/candidates")
public class CandidateAdminController {

    private final CandidateService candidateService;

    public CandidateAdminController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateResponse createCandidate(
            @PathVariable String electionCode,
            @Valid @RequestBody CreateCandidateRequest request) {

        return candidateService.createCandidate(
                electionCode,
                request
        );
    }

    @PutMapping("/{candidateCode}")
    public CandidateResponse updateCandidate(
            @PathVariable String electionCode,
            @PathVariable String candidateCode,
            @Valid @RequestBody UpdateCandidateRequest request) {

        return candidateService.updateCandidate(
                electionCode,
                candidateCode,
                request
        );
    }
}
