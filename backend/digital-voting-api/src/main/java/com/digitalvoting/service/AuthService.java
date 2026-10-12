package com.digitalvoting.service;

import com.digitalvoting.dto.LoginRequest;
import com.digitalvoting.dto.LoginResponse;
import com.digitalvoting.entity.Voter;
import com.digitalvoting.repository.VoterRepository;
import com.digitalvoting.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            VoterRepository voterRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.voterRepository = voterRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Voter voter = voterRepository
                .findByVoterId(request.getVoterId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid voter ID or password."
                        )
                );

        if (!"VERIFIED".equals(voter.getStatus())) {
            throw new IllegalArgumentException(
                    "Voter account is not verified."
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        voter.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Invalid voter ID or password."
            );
        }

        String token =
                jwtService.generateToken(
                        voter.getVoterId(),
                        voter.getRole()
                );

        return new LoginResponse(
                "Login successful.",
                voter.getVoterId(),
                voter.getStatus(),
                token
        );
    }
}
