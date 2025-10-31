package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ApiResponse;
import com.socialmedia.app.constants.GlobalConstants;
import com.socialmedia.app.dto.*;
import com.socialmedia.app.service.UserService;
import com.socialmedia.app.service.impl.AuthService;
import com.socialmedia.app.service.impl.OtpService;
import com.socialmedia.app.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin( origins = GlobalConstants.FRONTEND_URL )
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    public AuthController(AuthService authService, OtpService otpService, UserServiceImpl userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Step 1: Start registration -> Send OTP
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerStart(@Valid @RequestBody RegisterRequest req) {
        String token = authService.registerStart(req);
        Map<String, String> authToken = Map.of("authToken", token);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        "OTP sent successfully",
                        authToken,
                        HttpStatus.ACCEPTED.value()
                ));
    }

    /**
     * Step 2: Verify OTP and complete registration
     */
    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyAndComplete(@Valid @RequestBody VerifyOtpRequest req) {
        AuthResponse resp = authService.verifyOtpAndCompleteRegistration(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Verification successful",
                        resp,
                        HttpStatus.OK.value()
                ));
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse auth = authService.login(req);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        auth,
                        HttpStatus.OK.value()
                )
        );
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        AuthResponse refreshed = authService.refreshToken(req);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully",
                        refreshed,
                        HttpStatus.OK.value()
                )
        );
    }

    /**
     * Logout user (invalidate refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out successfully",
                        null,
                        HttpStatus.OK.value()
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgetPasswordRequest req) {
        String token = authService.forgotPassword(req);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("OTP sent for password reset", Map.of("authToken", token), HttpStatus.ACCEPTED.value()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.verifyOtpAndResetPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null, HttpStatus.OK.value()));
    }
    
    /**
     * Check if a username is available.
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = !userService.isUsernameTaken(username);
        return ResponseEntity.ok(
                ApiResponse.success("Username availability checked successfully", isAvailable, HttpStatus.OK.value())
        );
    }

    
}
