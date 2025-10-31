package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ApiResponse;
import com.socialmedia.app.model.Session;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Session>>> getMySessions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Session> sessions = sessionService.getActiveSessionsForUser(user);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched active sessions successfully", sessions, HttpStatus.OK.value())
        );
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<String>> terminateSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sessionId) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Ensure the session belongs to the authenticated user
        Session sessionToTerminate = sessionService.getActiveSessionsForUser(user).stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Session not found or does not belong to user"));

        sessionService.terminateSession(sessionId);
        return ResponseEntity.ok(
                ApiResponse.success("Session terminated successfully", null, HttpStatus.OK.value())
        );
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> terminateAllSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Authorization") String authorizationHeader) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String currentJwtToken = authorizationHeader.substring(7); // Extract token from "Bearer "
        Session currentSession = sessionService.getActiveSessionsForUser(user).stream()
                .filter(s -> s.getJwtToken().equals(currentJwtToken))
                .findFirst()
                .orElse(null);

        String currentSessionId = (currentSession != null) ? currentSession.getId() : null;

        sessionService.terminateAllSessions(user, currentSessionId);
        return ResponseEntity.ok(
                ApiResponse.success("All other sessions terminated successfully", null, HttpStatus.OK.value())
        );
    }
}
