package com.socialmedia.app.service;

import com.socialmedia.app.dto.UserProfileDTO;
import com.socialmedia.app.dto.UserProfileWithPostsDTO;

public interface ProfileService {
    UserProfileWithPostsDTO getUserProfileWithPosts(String targetUsername, String viewerUsername);
    UserProfileWithPostsDTO getUserProfileWithPostsByUsername(String username, String viewerUsername);
	UserProfileWithPostsDTO getUserProfileWithPostsByConversationId(String conversationId, String viewerId);
}
