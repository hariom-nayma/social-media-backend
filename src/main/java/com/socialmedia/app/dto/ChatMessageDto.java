package com.socialmedia.app.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String text;
    private String mediaUrl;
    private Instant createdAt;
    private boolean seen;
    private String fromUserId;
    private String toUserId;
	
}
