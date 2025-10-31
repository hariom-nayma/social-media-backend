package com.socialmedia.app.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender sender;
    public MailService(JavaMailSender sender) { this.sender = sender; }

    public void sendOtp(String to, String otp) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your verification code");
        msg.setText("Your OTP is: " + otp + "\nIt will expire in 5 minutes.");
        sender.send(msg);
    }
}

