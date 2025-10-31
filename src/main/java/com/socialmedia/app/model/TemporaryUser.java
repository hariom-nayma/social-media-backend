package com.socialmedia.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "temporary_users")
@Data
@NoArgsConstructor
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;
    
    private String firstName;
    
    private String lastName;

    private String password;

    private String otp;

    private Instant expiresAt;

    public TemporaryUser(String email, String username, String firstName, String lastName, String password, String otp, Instant expiresAt) {
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }
}
