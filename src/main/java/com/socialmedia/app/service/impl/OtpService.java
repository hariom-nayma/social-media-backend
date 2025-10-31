package com.socialmedia.app.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialmedia.app.model.OtpCode;
import com.socialmedia.app.repository.NewUserRepository;
import com.socialmedia.app.repository.OtpRepository;
import com.socialmedia.app.util.JwtUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final MailService mailService;
    private final Random random = new Random();
    private final JwtUtil jwtUtil;

    public OtpService(OtpRepository otpRepository, MailService mailService, JwtUtil jwtUtil) {
        this.otpRepository = otpRepository;
        this.mailService = mailService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String createAndSendOtp(String email) {
        // rate-limiting SHOULD be implemented outside (e.g., Redis) in prod
        String code = String.format("%06d", random.nextInt(1_000_000));
        var otp = new OtpCode(email, code, Instant.now().plus(5, ChronoUnit.MINUTES));
        otpRepository.save(otp);
        mailService.sendOtp(email, code);
        
        String token = jwtUtil.generateAuthToken(email);
        
        return token;
    }

    public boolean validateAndMarkUsed(String email, String code) {
        var o = otpRepository.findFirstByEmailAndCodeAndUsedFalseAndExpiresAtAfter(email, code, Instant.now());
        if (o.isEmpty()) return false;
        var db = o.get();
        db.setUsed(true);
        otpRepository.save(db);
        return true;
    }

    // cleanup expired OTPs - could be scheduled
    public void cleanupExpired() {
        otpRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}

