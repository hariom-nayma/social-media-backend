package com.socialmedia.app.service;

public interface JwtBlacklistService {
    void blacklistToken(String token);
    boolean isBlacklisted(String token);
}
