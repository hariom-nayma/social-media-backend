package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderUsername;
    private String senderFirstName;
    private String senderLastName;
    private String senderProfileImageUrl;
    private String content;
    private Instant sentAt;
    private boolean delivered;
    private boolean seen;
    private Instant seenAt;
    private List<ReactionDTO> reactions;
}
