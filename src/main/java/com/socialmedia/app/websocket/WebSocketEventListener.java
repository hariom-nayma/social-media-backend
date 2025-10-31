package com.socialmedia.app.websocket;

import com.socialmedia.app.dto.PresenceDTO;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.Optional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal principal = (Principal) headerAccessor.getSessionAttributes().get("simpUser");
        logger.info("Principal from session attributes: {}", principal);
        if (principal != null) {
            logger.info("Principal class: {}", principal.getClass());
            logger.info("Principal name: {}", principal.getName());
            String username = principal.getName();
            if(username != null) {
                logger.info("Attempting to find user with username: {}", username);
                Optional<User> userOptional = userRepository.findByUsername(username);
                logger.info("Result of userRepository.findByUsername({}): {}", username, userOptional);
                User user = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found in DB: " + username));

                // Explicitly initialize collections
                user.getFollowers().size();
                user.getFollowing().size();

                user.setOnline(true);
                userRepository.save(user);
                broadcastPresence(user, true);
            }
        } else {
            logger.warn("Principal not found in session attributes. Cannot process WebSocket connection.");
        }
    }

    @EventListener
    @Transactional
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal principal = (Principal) headerAccessor.getSessionAttributes().get("simpUser");
        if(principal != null) {
            String username = principal.getName();
            if(username != null) {
                logger.info("Attempting to find user with username: {}", username);
                Optional<User> userOptional = userRepository.findByUsername(username);
                logger.info("Result of userRepository.findByUsername({}): {}", username, userOptional);
                User user = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found in DB: " + username));

                // Explicitly initialize collections
                user.getFollowers().size();
                user.getFollowing().size();

                user.setOnline(false);
                userRepository.save(user);
                broadcastPresence(user, false);
            }
        }
    }

    private void broadcastPresence(User user, boolean isOnline) {
        PresenceDTO presenceDTO = new PresenceDTO(user.getId(), isOnline);
        user.getFollowers().forEach(follower -> {
            messagingTemplate.convertAndSendToUser(follower.getUsername(), "/queue/presence", presenceDTO);
        });
        user.getFollowing().forEach(following -> {
            messagingTemplate.convertAndSendToUser(following.getUsername(), "/queue/presence", presenceDTO);
        });
    }
}
