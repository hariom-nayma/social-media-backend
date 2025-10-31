package com.socialmedia.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoryDTO {
    private Long id;
    private String contentUrl;
    private String caption;
    private LocalDateTime createdAt;
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private boolean likedByme;
    private boolean viewedByMe;
}