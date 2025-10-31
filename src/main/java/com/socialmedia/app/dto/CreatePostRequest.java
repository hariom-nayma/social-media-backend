package com.socialmedia.app.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePostRequest {
    private String content;
    private Boolean isPublic;
    private MultipartFile media;
}

