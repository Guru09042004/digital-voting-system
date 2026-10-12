package com.digitalvoting.controller;

import com.digitalvoting.dto.CastVoteRequest;
import com.digitalvoting.dto.VoteResponse;
import com.digitalvoting.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voter/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public VoteResponse castVote(
            Authentication authentication,
            @Valid @RequestBody CastVoteRequest request) {
        return voteService.castVote(authentication.getName(), request);
    }
}
