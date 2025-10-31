package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ApiResponse;
import com.socialmedia.app.dto.UserProfileWithPostsDTO;
import com.socialmedia.app.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping({"/{id}", ""})
    public ResponseEntity<ApiResponse<?>> getUserProfileWithPosts(@PathVariable(required = false) String id,
                                                                           @AuthenticationPrincipal UserDetails user) {
        String viewerId = user.getUsername();
        String targetId = (id != null && !id.isEmpty()) ? id : viewerId;
        return ResponseEntity.ok(ApiResponse.success("Profile fetched Successfully", profileService.getUserProfileWithPosts(targetId, viewerId), 200));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<?>> getUserProfileWithPostsByUsername(@PathVariable String username,
                                                                                   @AuthenticationPrincipal UserDetails user) {
        String viewerId = user.getUsername();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched Successfully", profileService.getUserProfileWithPostsByUsername(username, viewerId), 200));
    }
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getUserProfileWithPostsByConversationId(@PathVariable String conversationId,
																				   @AuthenticationPrincipal UserDetails user) {
		String viewerId = user.getUsername();
		return ResponseEntity.ok(ApiResponse.success("Profile fetched Successfully", profileService.getUserProfileWithPostsByConversationId(conversationId, viewerId), 200));
	}
}
