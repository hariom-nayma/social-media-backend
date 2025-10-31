package com.socialmedia.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "sessions")
@Getter @Setter @NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true, length = 500) // Store JWT token or its hash
    private String jwtToken;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 50)
    private String deviceType;

    @Column(length = 50)
    private String os;

    @Column(length = 50)
    private String browser;

    @Column(nullable = false)
    private Instant firstLogin;

    @Column(nullable = false)
    private Instant lastActive;

    @Column(nullable = false)
    private boolean active = true;

    // Constructor for convenience
    public Session(User user, String jwtToken, String ipAddress, String location, String userAgent,
                   String deviceType, String os, String browser, Instant firstLogin, Instant lastActive) {
        this.user = user;
        this.jwtToken = jwtToken;
        this.ipAddress = ipAddress;
        this.location = location;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.os = os;
        this.browser = browser;
        this.firstLogin = firstLogin;
        this.lastActive = lastActive;
        this.active = true;
    }
}
