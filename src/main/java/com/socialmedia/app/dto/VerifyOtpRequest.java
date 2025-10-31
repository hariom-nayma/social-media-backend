package com.socialmedia.app.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String token;
    private String otp;
}
