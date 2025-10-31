package com.socialmedia.app.service;


import com.socialmedia.app.dto.UpdateProfileRequest;
import com.socialmedia.app.dto.UserDTO;
import com.socialmedia.app.model.FollowRequest;
import com.socialmedia.app.model.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO getMyProfile(String username);
    UserDTO updateMyProfile(String username, UpdateProfileRequest req);
    UserDTO getUserProfile(String targetUsername, String viewerUsername);
    String followUser(String followerUsername, String targetUsername);
    String unfollowUser(String followerUsername, String targetUsername);
    List<UserDTO> getFollowers(String username);
    List<UserDTO> getFollowing(String username);
    List<User> getAllUsers();
    String acceptFollowRequest(String username, Long requestId);
    String declineFollowRequest(String username, Long requestId);
    List<com.socialmedia.app.dto.FollowRequestDTO> getPendingFollowRequests(String username);
    List<com.socialmedia.app.dto.FriendSuggestionDTO> getFriendSuggestions(String username);
    List<com.socialmedia.app.dto.UserRelationshipDTO> getFollowersWithDetails(String targetUsername, String viewerUsername);
    List<com.socialmedia.app.dto.UserRelationshipDTO> getFollowingWithDetails(String targetUsername, String viewerUsername);
    boolean isUsernameTaken(String username);
}


