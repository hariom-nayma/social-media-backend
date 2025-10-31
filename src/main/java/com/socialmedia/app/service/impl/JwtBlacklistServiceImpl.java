package com.socialmedia.app.service.impl;

import com.socialmedia.app.service.JwtBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
