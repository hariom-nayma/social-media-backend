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
public class FriendSuggestionDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private String profileUrl;
    private boolean isFollowMe; // Indicates if the suggested user follows the current user
    private boolean isPrivate; // Indicates if the suggested user's account is private
    private boolean isRequested; // Indicates if a follow request has been sent to this user
    private long mutualCount;   // Count of mutual followers
}
