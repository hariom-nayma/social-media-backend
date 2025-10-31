package com.socialmedia.app.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


public class UserProfileWithPostsDTO extends UserProfileDTO {


	private List<FeedPostResponseDTO> posts;
    private boolean isFollowing; // Indicates if the viewer is following the target user
    private boolean isRequested; // Indicates if the viewer has sent a follow request to a private target user

    public UserProfileWithPostsDTO() {
    			super();
    }


    public List<FeedPostResponseDTO> getPosts() {
        return posts;
    }

    public void setPosts(List<FeedPostResponseDTO> posts) {
        this.posts = posts;
    }
    
    public boolean isFollowing() {
		return isFollowing;
	}
    	public void setFollowing(boolean isFollowing) {
		this.isFollowing = isFollowing;
	}
    		public boolean isRequested() {
		return isRequested;
	}
			public void setRequested(boolean isRequested) {
		this.isRequested = isRequested;
	}
}
