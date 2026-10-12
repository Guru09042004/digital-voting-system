package com.digitalvoting.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final long OTP_EXPIRY_MINUTES = 5;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndStoreOtp(String voterId) {

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        String key = "voting:otp:" + voterId;

        redisTemplate.opsForValue().set(
                key,
                otp,
                OTP_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );

        return otp;
    }

    public boolean verifyOtp(String voterId, String enteredOtp) {

        String key = "voting:otp:" + voterId;

        String storedOtp =
                redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            return false;
        }

        if (!storedOtp.equals(enteredOtp)) {
            return false;
        }

        // OTP can be used only once
        redisTemplate.delete(key);

        return true;
    }
}
