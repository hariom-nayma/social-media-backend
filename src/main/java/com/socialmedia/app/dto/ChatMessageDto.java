package com.socialmedia.app.dto;

import lombok.*;
import java.time.Instant;

 @Data @AllArgsConstructor @NoArgsConstructor
public class ChatMessageDto {
    private String conversationId;
    private String senderId;
    private String recipientId;
    private String content;
    private Instant timestamp = Instant.now();
}