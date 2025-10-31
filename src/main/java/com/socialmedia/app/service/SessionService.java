package com.socialmedia.app.service;

import com.socialmedia.app.model.Session;
import com.socialmedia.app.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SessionService {
    Session createSession(User user, String jwtToken, HttpServletRequest request);
    List<Session> getActiveSessionsForUser(User user);
    void terminateSession(String sessionId);
    void terminateAllSessions(User user, String currentSessionId);
    void updateLastActiveTime(String jwtToken);
    void invalidateSession(String jwtToken);
}
