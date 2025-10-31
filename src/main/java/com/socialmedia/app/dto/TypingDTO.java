package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingDTO {
    private String senderId;
    private String recipientId;
    private boolean typing;
}
