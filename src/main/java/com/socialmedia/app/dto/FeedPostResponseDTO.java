package com.socialmedia.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class FeedPostResponseDTO {
    private String id;
    private String content;
    private String mediaUrl;
    private boolean isPublic;
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String profileImage;
    private Instant createdAt;
    private int likeCount;
    private int commentsCount;
    private boolean likedByCurrentUser;
    private boolean savedByMe;
//    private List<CommentDTO> comments;
}
