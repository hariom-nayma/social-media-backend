package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.*;
import com.socialmedia.app.enums.TokenType;
import com.socialmedia.app.enums.UserRole;
import com.socialmedia.app.exception.CustomException;
import com.socialmedia.app.exception.TokenNotFoundExceprion;
import com.socialmedia.app.model.*;
import com.socialmedia.app.repository.*;
import com.socialmedia.app.util.JwtUtil;

import com.socialmedia.app.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NewUserRepository newUserRepository;
    private final TemporaryUserRepository temporaryUserRepository;
    private final MailService mailService;
    private final SessionService sessionService;
    private final HttpServletRequest request;

    public AuthService(UserRepository userRepository,
                       OtpService otpService,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshTokenRepository,
                       NewUserRepository newUserRepository,
                       TemporaryUserRepository temporaryUserRepository,
                       MailService mailService,
                       SessionService sessionService,
                       HttpServletRequest request) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.newUserRepository = newUserRepository;
        this.temporaryUserRepository = temporaryUserRepository;
        this.mailService = mailService;
        this.sessionService = sessionService;
        this.request = request;
    }

    /**
     * Step 1 — Start registration: send OTP & issue auth token
     */
    public String registerStart(RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Delete old temporary user if exists
        temporaryUserRepository.findByEmail(req.getEmail()).ifPresent(temporaryUserRepository::delete);

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        Instant expiry = Instant.now().plus(10, ChronoUnit.MINUTES);

        TemporaryUser temp = new TemporaryUser(
                req.getEmail(),
                req.getUsername(),
                req.getFirstName(),
                req.getLastName(),
                passwordEncoder.encode(req.getPassword()),
                otp,
                expiry
        );
        temporaryUserRepository.save(temp);

        // Generate auth token for OTP verification
        String token = jwtUtil.generateAuthToken(req.getUsername());

        mailService.sendOtp(req.getEmail(), otp);

        return token;
    }

    /**
     * Step 2 — Verify OTP and create real user
     */
    @Transactional
    public AuthResponse verifyOtpAndCompleteRegistration(VerifyOtpRequest req) {
        if (!jwtUtil.isTokenType(req.getToken(), TokenType.AUTH)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        String username = jwtUtil.getUsername(req.getToken());
        TemporaryUser tempUser = temporaryUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (tempUser.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        if (!tempUser.getOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Create verified user
        User user = new User();
        user.setEmail(tempUser.getEmail());
        user.setUsername(tempUser.getUsername());
        user.setFirstName(tempUser.getFirstName());
        user.setLastName(tempUser.getLastName());
        user.setPassword(tempUser.getPassword());
        user.setVerified(true);
        user.setRoles(Set.of(UserRole.USER));
        user.setProfileImageUrl("https://res.cloudinary.com/dao4ty0ac/image/upload/v1761713060/blank-profile-picture-973460_1280_1_cmkabx.webp");
        userRepository.save(user);

        // Delete temporary user after success
        temporaryUserRepository.delete(tempUser);

        // Generate tokens
        String access = jwtUtil.generateToken(user.getUsername(), TokenType.ACCESS, List.of("USER"));
        String refresh = jwtUtil.generateToken(user.getUsername(), TokenType.REFRESH, List.of("USER"));

        saveRefreshToken(user, refresh);

        return new AuthResponse(access, refresh);
    }

    /**
     * Login existing user with email/password
     */
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String access = jwtUtil.generateToken(user.getUsername(), TokenType.ACCESS, List.of("USER"));
        String refresh = jwtUtil.generateToken(user.getUsername(), TokenType.REFRESH, List.of("USER"));

        saveRefreshToken(user, refresh);
        sessionService.createSession(user, access, request);

        return new AuthResponse(access, refresh);
    }

    /**
     * Refresh access/refresh token pair
     */
    @Transactional
    public AuthResponse refreshToken(RefreshRequest req) {
        RefreshToken dbToken = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new TokenNotFoundExceprion("Refresh token not found"));

        if (dbToken.isRevoked() || dbToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenNotFoundExceprion("Refresh token invalid or expired");
        }

        if (!jwtUtil.isTokenType(req.getRefreshToken(), TokenType.REFRESH)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        String username = jwtUtil.getUsername(req.getRefreshToken());
        User user = dbToken.getUser();
        if (!user.getUsername().equals(username)) {
            throw new RuntimeException("Token user mismatch");
        }

        // Revoke old token
        dbToken.setRevoked(true);
        refreshTokenRepository.save(dbToken);

        String access = jwtUtil.generateToken(user.getUsername(), TokenType.ACCESS, List.of("USER"));
        String refresh = jwtUtil.generateToken(user.getUsername(), TokenType.REFRESH, List.of("USER"));
        saveRefreshToken(user, refresh);
        sessionService.createSession(user, access, request);

        return new AuthResponse(access, refresh);
    }

    /**
     * Logout: revoke refresh token
     */
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
        sessionService.invalidateSession(refreshToken);
    }

    /**
     * Helper — Save refresh token
     */
    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(
                Instant.now().plusMillis(
                        Long.parseLong(System.getProperty("app.jwt.refreshExpirationMs", "1209600000"))
                )
        );
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Step 1 — Forgot Password: send OTP and temporary auth token
     */
    public String forgotPassword(ForgetPasswordRequest req) {
        var user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException("No account found with this email"));

        // Clean any old temporary reset record
        temporaryUserRepository.findByEmail(req.getEmail()).ifPresent(temporaryUserRepository::delete);

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        Instant expiry = Instant.now().plus(10, ChronoUnit.MINUTES);

        // Store temporary reset request
        TemporaryUser temp = new TemporaryUser(
                req.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(), // existing password (not changed yet)
                otp,
                expiry
        );
        temporaryUserRepository.save(temp);

        // Generate short-lived token for password reset
        String token = jwtUtil.generateAuthToken(user.getUsername());

        mailService.sendOtp(req.getEmail(), otp);

        return token;
    }

    /**
     * Step 2 — Verify OTP and Reset Password
     */
    @Transactional
    public void verifyOtpAndResetPassword(ResetPasswordRequest req) {
        if (!jwtUtil.isTokenType(req.getToken(), TokenType.AUTH)) {
            throw new CustomException("Invalid token type");
        }

        String username = jwtUtil.getUsername(req.getToken());
        TemporaryUser tempUser = temporaryUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("Invalid or expired reset token"));

        if (tempUser.getExpiresAt().isBefore(Instant.now())) {
            throw new CustomException("Reset token has expired");
        }

        if (!tempUser.getOtp().equals(req.getOtp())) {
            throw new CustomException("Invalid OTP");
        }

        // Update real user password
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // Clean temporary record
        temporaryUserRepository.delete(tempUser);
    }

}
