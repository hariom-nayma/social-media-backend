package com.socialmedia.app.dto;

import com.socialmedia.app.enums.NotificationType;

import lombok.Data;

import java.time.Instant;

@Data
public class NotificationDto {
    private String id;
    private NotificationType type;
    private String message;
    private boolean read;
    private Instant createdAt;
    private String payload;
}
