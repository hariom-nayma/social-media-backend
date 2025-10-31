package com.socialmedia.app.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImageUrl;
    private boolean isPrivate;
    private boolean verified;
    private int followersCount;
    private int followingCount;
}

