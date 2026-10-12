package com.digitalvoting.controller;

import com.digitalvoting.dto.LoginRequest;
import com.digitalvoting.dto.LoginResponse;
import com.digitalvoting.dto.RegisterRequest;
import com.digitalvoting.dto.RegisterResponse;
import com.digitalvoting.dto.VerifyOtpRequest;
import com.digitalvoting.dto.VerifyOtpResponse;
import com.digitalvoting.service.AuthService;
import com.digitalvoting.service.VoterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final VoterService voterService;
    private final AuthService authService;

    public AuthController(
            VoterService voterService,
            AuthService authService) {

        this.voterService = voterService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return voterService.register(request);
    }

    @PostMapping("/verify-otp")
    public VerifyOtpResponse verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return voterService.verifyOtp(
                request.getVoterId(),
                request.getOtp()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @PostMapping("/dev/regenerate-otp")
    public String regenerateOtp(@RequestParam String voterId) {
        return voterService.regenerateOtpForPendingVoter(voterId);
    }
}
