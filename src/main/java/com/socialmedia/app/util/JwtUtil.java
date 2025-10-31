package com.socialmedia.app.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.socialmedia.app.enums.TokenType;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessMs;
    private final long refreshMs;

    private final long authMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessExpirationMs}") long accessMs,
            @Value("${app.jwt.refreshExpirationMs}") long refreshMs,
            @Value("${app.jwt.authExpirationMs}") long authMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
        this.authMs = authMs;
    }

    public String generateToken(String username, TokenType type, List<String> roles) {
        long ttl = type == TokenType.ACCESS ? accessMs : refreshMs;
        Date now = Date.from(Instant.now());
        return Jwts.builder()
                .setSubject(username)
                .claim("type", type.name())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttl))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAuthToken(String username) {
        Date now = Date.from(Instant.now());
        return Jwts.builder()
                .setSubject(username)
                .claim("type", TokenType.AUTH.name())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + authMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        Claims c = parseClaims(token).getBody();
        return c.getSubject();
    }

    public Jws<Claims> parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String getUsername(String token) {
        Claims c = parseClaims(token).getBody();
        return c.getSubject();
    }

    public boolean isTokenType(String token, TokenType expected) {
        Claims c = parseClaims(token).getBody();
        String type = c.get("type", String.class);
        return expected.name().equals(type);
    }
}

