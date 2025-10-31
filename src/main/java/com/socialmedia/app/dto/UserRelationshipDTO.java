package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelationshipDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private String profileImageUrl;
    private boolean isPrivate;
    private boolean isRequested; // If the viewer has sent a follow request to this user
    private boolean isFollowing; // If the viewer is following this user
    private long mutualCount;   // Mutual followers with the viewer
}
