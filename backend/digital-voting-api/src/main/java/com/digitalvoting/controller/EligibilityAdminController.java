package com.digitalvoting.controller;

import com.digitalvoting.dto.EligibilityRequest;
import com.digitalvoting.entity.VoterEligibility;
import com.digitalvoting.service.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/elections")
public class EligibilityAdminController {

    private final EligibilityService eligibilityService;

    public EligibilityAdminController(
            EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PutMapping("/{electionCode}/eligibility")
    public VoterEligibility updateEligibility(
            @PathVariable String electionCode,
            @Valid @RequestBody EligibilityRequest request) {

        return eligibilityService.updateEligibility(
                electionCode,
                request
        );
    }
}
