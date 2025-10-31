package com.socialmedia.app.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String bio;
    private Boolean isPrivate;
    private String profileImageUrl; // to be replaced by Cloudinary upload later
}
