package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationListDTO {
    private String conversationId;
    private String otherUserId;
    private String otherUsername;
    private String otherFirstName;
    private String otherLastName;
    private String otherProfileImageUrl;
    private String lastMessageContent;
    private Instant lastMessageTimestamp;
    private int unreadMessageCount;
}
