package com.digitalvoting.service;

import com.digitalvoting.dto.RegisterRequest;
import com.digitalvoting.dto.RegisterResponse;
import com.digitalvoting.dto.VerifyOtpResponse;
import com.digitalvoting.entity.Voter;
import com.digitalvoting.repository.VoterRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class VoterService {

    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public VoterService(
            VoterRepository voterRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {

        this.voterRepository = voterRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public RegisterResponse register(RegisterRequest request) {

        if (voterRepository.existsByVoterId(request.getVoterId())) {
            throw new IllegalArgumentException(
                    "Voter ID already registered."
            );
        }

        if (voterRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException(
                    "Mobile number already registered."
            );
        }

        if (voterRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered."
            );
        }

        Voter voter = new Voter();

        voter.setVoterId(request.getVoterId());
        voter.setFullName(request.getFullName());
        voter.setDateOfBirth(request.getDateOfBirth());
        voter.setMobile(request.getMobile());
        voter.setEmail(request.getEmail());

        voter.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        voter.setStatus("PENDING");

        voterRepository.save(voter);

        // Generate OTP and store it in Redis
        String otp = otpService.generateAndStoreOtp(
                voter.getVoterId()
        );

        // Temporary development output.
        // Later this will be replaced with Email/SMS delivery.
        System.out.println(
                "========================================"
        );
        System.out.println(
                "OTP for " + voter.getVoterId() + " = " + otp
        );
        System.out.println(
                "OTP expires in 5 minutes"
        );
        System.out.println(
                "========================================"
        );

        return new RegisterResponse(
                "Registration successful. OTP verification is pending.",
                voter.getVoterId(),
                voter.getStatus()
        );
    }

    public VerifyOtpResponse verifyOtp(
            String voterId,
            String otp) {

        Voter voter = voterRepository
                .findByVoterId(voterId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Voter not found."
                        )
                );

        if ("VERIFIED".equals(voter.getStatus())) {
            throw new IllegalArgumentException(
                    "Voter is already verified."
            );
        }

        boolean valid = otpService.verifyOtp(
                voterId,
                otp
        );

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid or expired OTP."
            );
        }

        voter.setStatus("VERIFIED");

        voterRepository.save(voter);

        return new VerifyOtpResponse(
                "OTP verified successfully.",
                voter.getVoterId(),
                voter.getStatus()
        );
    }

    public String regenerateOtpForPendingVoter(String voterId) {
        Voter voter = voterRepository.findByVoterId(voterId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Voter not found."));

        if ("VERIFIED".equals(voter.getStatus())) {
            throw new IllegalArgumentException("Voter is already verified.");
        }

        String otp = otpService.generateAndStoreOtp(voterId);
        System.out.println("Development OTP for " + voterId + " = " + otp);

        return "OTP regenerated. Check backend logs for the development OTP.";
    }
}
