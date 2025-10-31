package com.socialmedia.app.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialmedia.app.model.Session;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.SessionRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.JwtBlacklistService;
import com.socialmedia.app.service.SessionService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    public SessionServiceImpl(SessionRepository sessionRepository, UserRepository userRepository, JwtBlacklistService jwtBlacklistService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    public Session createSession(User user, String jwtToken, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Basic User-Agent parsing
        String deviceType = "Unknown";
        String os = "Unknown";
        String browser = "Unknown";

        if (userAgent != null) {
            if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                deviceType = "Mobile";
            } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
                deviceType = "Tablet";
            } else {
                deviceType = "Desktop";
            }

            if (userAgent.contains("Windows")) os = "Windows";
            else if (userAgent.contains("Mac")) os = "macOS";
            else if (userAgent.contains("Linux")) os = "Linux";
            else if (userAgent.contains("Android")) os = "Android";
            else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";

            if (userAgent.contains("Chrome")) browser = "Chrome";
            else if (userAgent.contains("Firefox")) browser = "Firefox";
            else if (userAgent.contains("Safari")) browser = "Safari";
            else if (userAgent.contains("Edge")) browser = "Edge";
        }

        // For location, we'll just store the IP address for now.
        // A proper geolocation service would be integrated here.
        String location = "IP: " + ipAddress; 

        Session session = new Session(user, jwtToken, ipAddress, location, userAgent,
                deviceType, os, browser, Instant.now(), Instant.now());
        return sessionRepository.save(session);
    }

    @Override
    public List<Session> getActiveSessionsForUser(User user) {
        return sessionRepository.findByUserAndActiveTrue(user);
    }

    @Override
    public void terminateSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setActive(false);
        sessionRepository.save(session);
        jwtBlacklistService.blacklistToken(session.getJwtToken());
    }

    @Override
    public void terminateAllSessions(User user, String currentSessionId) {
        List<Session> activeSessions = sessionRepository.findByUserAndActiveTrue(user);
        for (Session session : activeSessions) {
            if (!session.getId().equals(currentSessionId)) {
                session.setActive(false);
                sessionRepository.save(session);
                jwtBlacklistService.blacklistToken(session.getJwtToken());
            }
        }
    }

    @Override
    public void updateLastActiveTime(String jwtToken) {
        sessionRepository.findByJwtTokenAndActiveTrue(jwtToken).ifPresent(session -> {
            session.setLastActive(Instant.now());
            sessionRepository.save(session);
        });
    }

    @Override
    public void invalidateSession(String jwtToken) {
        sessionRepository.findByJwtTokenAndActiveTrue(jwtToken).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
            jwtBlacklistService.blacklistToken(session.getJwtToken());
        });
    }
}
