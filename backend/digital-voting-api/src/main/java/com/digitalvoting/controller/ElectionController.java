package com.digitalvoting.controller;

import com.digitalvoting.dto.ElectionResponse;
import com.digitalvoting.service.ElectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping("/{electionCode}")
    public ElectionResponse getElection(
            @PathVariable String electionCode) {

        return electionService.getElection(electionCode);
    }

    @GetMapping
    public List<ElectionResponse> getAllElections() {

        return electionService.getAllElections();
    }
}
