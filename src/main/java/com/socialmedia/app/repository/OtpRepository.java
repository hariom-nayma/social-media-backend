package com.socialmedia.app.repository;


import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.OtpCode;

public interface OtpRepository extends JpaRepository<OtpCode, String> {
    Optional<OtpCode> findFirstByEmailAndCodeAndUsedFalseAndExpiresAtAfter(String email, String code, Instant now);
    void deleteAllByExpiresAtBefore(Instant cutOff);
}
