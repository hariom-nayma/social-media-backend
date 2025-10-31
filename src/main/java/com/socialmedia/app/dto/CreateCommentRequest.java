package com.socialmedia.app.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String text;
    private String parentCommentId;
}

