package com.socialmedia.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import com.socialmedia.app.enums.TokenType;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(columnList = "token")
})
@Data
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked = false;

    @Enumerated(EnumType.STRING)
    private TokenType type = TokenType.REFRESH;
}

