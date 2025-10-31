package com.socialmedia.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Set;

@Data
public class CommentDTO {
    private String id;
    private String text;
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String userProfileImage;
    private Instant createdAt;
    private Integer likesCount;
    private boolean likedByCurrentUser;
    private String parentCommentId;
    private Set<CommentDTO> replies;
}
