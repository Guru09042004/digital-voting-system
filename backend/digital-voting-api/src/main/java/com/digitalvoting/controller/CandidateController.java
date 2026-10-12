package com.digitalvoting.controller;

import com.digitalvoting.dto.CandidateResponse;
import com.digitalvoting.service.CandidateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elections/{electionCode}/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(
            CandidateService candidateService) {

        this.candidateService = candidateService;
    }

    @GetMapping
    public List<CandidateResponse> getCandidates(
            @PathVariable String electionCode) {

        return candidateService.getCandidates(electionCode);
    }
}
