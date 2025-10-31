package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
	private String id;
    private String profileImageUrl; 
	private String username;
	private String firstName;
	private String lastName;
	private String bio;
	private Boolean isPrivate;
    private int followersCount;
    private int followingCount;
}
