package com.socialmedia.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Data
public class PostDTO {
    private String id;
    private String content;
    private String mediaUrl;
    private boolean isPublic;
    private String userId;
    private String username;
    private int likeCount;
    private List<CommentDTO> comments;
}
