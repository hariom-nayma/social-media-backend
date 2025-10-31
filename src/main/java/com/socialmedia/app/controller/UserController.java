package com.socialmedia.app.controller;

import com.socialmedia.app.dto.FollowRequestDTO;
import com.socialmedia.app.dto.UpdateProfileRequest;
import com.socialmedia.app.dto.UserDTO;
import com.socialmedia.app.dto.UserRelationshipDTO;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.dto.ApiResponse;
import com.socialmedia.app.service.UserService;
import com.socialmedia.app.service.impl.CloudinaryService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;


    public UserController(UserService userService, CloudinaryService cloudinaryService, UserRepository userRepository) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Fetched all users successfully", users, HttpStatus.OK.value())
        );
    }

    /**
     * Get logged-in user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile(@AuthenticationPrincipal UserDetails user) {
        UserDTO profile = userService.getMyProfile(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Fetched profile successfully", profile, HttpStatus.OK.value())
        );
    }

    /**
     * Update logged-in user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody UpdateProfileRequest req) {

        UserDTO updated = userService.updateMyProfile(user.getUsername(), req);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updated, HttpStatus.OK.value())
        );
    }

    /**
     * Get another user's profile (public/private)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {

        UserDTO profile = userService.getUserProfile(id, user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Fetched user profile successfully", profile, HttpStatus.OK.value())
        );
    }

    /**
     * Follow another user
     */
    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<String>> followUser(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id) {

        String message = userService.followUser(user.getUsername(), id);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Unfollow a user
     */
    @PostMapping("/{id}/unfollow")
    public ResponseEntity<ApiResponse<String>> unfollowUser(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id) {

        String message = userService.unfollowUser(user.getUsername(), id);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Get followers of a user (with detailed relationship info)
     */
    @GetMapping({"/followers", "/{username}/followers"})
    public ResponseEntity<ApiResponse<List<UserRelationshipDTO>>> getFollowersWithDetails(
            @PathVariable(required = false) String username,
            @AuthenticationPrincipal UserDetails user) {

        String viewerId = user.getUsername();
        String targetUserId = (username != null && !username.isEmpty()) ? 
                userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found")).getId() : viewerId;

        List<UserRelationshipDTO> followers = userService.getFollowersWithDetails(targetUserId, viewerId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched followers successfully", followers, HttpStatus.OK.value())
        );
    }

    /**
     * Get users the given user is following (with detailed relationship info)
     */
    @GetMapping({"/following", "/{username}/following"})
    public ResponseEntity<ApiResponse<List<UserRelationshipDTO>>> getFollowingWithDetails(
            @PathVariable(required = false) String username,
            @AuthenticationPrincipal UserDetails user) {

        String viewerId = user.getUsername();
        String targetUserId = (username != null && !username.isEmpty()) ? 
                userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found")).getId() : viewerId;

        List<UserRelationshipDTO> following = userService.getFollowingWithDetails(targetUserId, viewerId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched following users successfully", following, HttpStatus.OK.value())
        );
    }

    /**
     * Upload profile image
     */
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = cloudinaryService.uploadImage(file);

        UserDTO updated = userService.updateMyProfile(user.getUsername(), new UpdateProfileRequest() {{
            setProfileImageUrl(imageUrl);
        }});

        Map<String, String> result = Map.of(
                "imageUrl", imageUrl,
                "message", "Profile image updated successfully"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Profile image updated", result, HttpStatus.OK.value())
        );
    }

    /**
     * Delete profile image
     */
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<String>> deleteProfileImage(@AuthenticationPrincipal UserDetails user) {
        UserDTO me = userService.getMyProfile(user.getUsername());
        String url = me.getProfileImageUrl();

        if (url != null && url.contains("/upload/")) {
            String publicId = url.substring(url.indexOf("/upload/") + 8, url.lastIndexOf('.'));
            cloudinaryService.deleteImage(publicId);
        }

        userService.updateMyProfile(user.getUsername(), new UpdateProfileRequest() {{
            setProfileImageUrl(null);
        }});

        return ResponseEntity.ok(
                ApiResponse.success("Profile image removed successfully", null, HttpStatus.OK.value())
        );
    }

    /**
     * Get pending follow requests for the logged-in user
     */
    @GetMapping("/follow-requests")
    public ResponseEntity<ApiResponse<List<FollowRequestDTO>>> getPendingFollowRequests(
            @AuthenticationPrincipal UserDetails user) {

        List<FollowRequestDTO> requests = userService.getPendingFollowRequests(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Fetched follow requests successfully", requests, HttpStatus.OK.value())
        );
    }

    /**
     * Accept a follow request
     */
    @PostMapping("/follow-requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptFollowRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long requestId) {

        String message = userService.acceptFollowRequest(user.getUsername(), requestId);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Decline a follow request
     */
    @PostMapping("/follow-requests/{requestId}/decline")
    public ResponseEntity<ApiResponse<String>> declineFollowRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long requestId) {

        String message = userService.declineFollowRequest(user.getUsername(), requestId);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Get friend suggestions for the logged-in user
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<com.socialmedia.app.dto.FriendSuggestionDTO>>> getFriendSuggestions(
            @AuthenticationPrincipal UserDetails user) {

        List<com.socialmedia.app.dto.FriendSuggestionDTO> suggestions = userService.getFriendSuggestions(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Fetched friend suggestions successfully", suggestions, HttpStatus.OK.value())
        );
    }

    /**
     * Check if a username is available.
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = !userService.isUsernameTaken(username);
        return ResponseEntity.ok(
                ApiResponse.success("Username availability checked successfully", isAvailable, HttpStatus.OK.value())
        );
    }
}
