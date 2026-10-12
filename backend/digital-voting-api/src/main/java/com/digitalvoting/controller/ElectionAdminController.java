package com.digitalvoting.controller;

import com.digitalvoting.dto.CreateElectionRequest;
import com.digitalvoting.dto.ElectionResponse;
import com.digitalvoting.dto.UpdateElectionStatusRequest;
import com.digitalvoting.service.ElectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/elections")
public class ElectionAdminController {

    private final ElectionService electionService;

    public ElectionAdminController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ElectionResponse createElection(
            @Valid @RequestBody CreateElectionRequest request) {

        return electionService.createElection(request);
    }

    @PutMapping("/{electionCode}/status")
    public ElectionResponse updateStatus(
            @PathVariable String electionCode,
            @Valid @RequestBody UpdateElectionStatusRequest request) {

        return electionService.updateStatus(
                electionCode,
                request.getStatus()
        );
    }
}
