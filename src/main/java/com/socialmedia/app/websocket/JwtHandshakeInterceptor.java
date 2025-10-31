package com.socialmedia.app.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;

import com.socialmedia.app.enums.TokenType;
import com.socialmedia.app.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;


import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intercepts the handshake, extracts token from "Authorization" header or "token" query param,
 * validates it and sets Principal with userId so SimpMessagingTemplate.convertAndSendToUser()
 * can target the correct session.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

	@Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String token = null;
        // 1. Authorization header
        logger.info("Attempting to extract token from 'Authorization' header...");
        if (request.getHeaders().containsKey("Authorization")) {
            List<String> auth = request.getHeaders().get("Authorization");
            if (auth != null && !auth.isEmpty()) {
                String h = auth.get(0);
                if (h.startsWith("Bearer ")) {
                    token = h.substring(7);
                    logger.info("Token found in 'Authorization' header.");
                }
            }
        }

        // 2. token query param fallback
        if (token == null) {
            logger.info("Token not found in 'Authorization' header, checking 'token' query parameter...");
            if (request instanceof ServletServerHttpRequest servlet) {
                HttpServletRequest req = servlet.getServletRequest();
                String t = req.getParameter("token");
                if (t != null && !t.isBlank()) {
                    token = t;
                    logger.info("Token found in 'token' query parameter.");
                }
            }
        }

        if (token == null) {
            logger.warn("No token found in 'Authorization' header or 'token' query parameter. Rejecting handshake.");
            return false;
        }

        if (jwtUtil == null) {
            logger.error("JwtUtil is null. Rejecting handshake.");
            return false;
        }

        try {
            logger.info("Validating token type...");
            if (!jwtUtil.isTokenType(token, TokenType.ACCESS)) {
                logger.warn("Invalid token type. Rejecting handshake.");
                return false;
            }
            logger.info("Token type is valid. Extracting username...");
            var username = jwtUtil.getUsername(token);
            logger.info("Username '{}' extracted from token. Setting principal.", username);
            // set Principal name as username string per handshake
            attributes.put("simpUser", new StompPrincipal(username));
            logger.info("Handshake successful.");
            return true;
        } catch (Exception ex) {
            logger.error("Exception during handshake: {}", ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                         WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("Exception after handshake: {}", exception.getMessage());
        }
    }
}
