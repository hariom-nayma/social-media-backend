package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionDTO {
    private String messageId;
    private String reaction;
    private String userId;
}
