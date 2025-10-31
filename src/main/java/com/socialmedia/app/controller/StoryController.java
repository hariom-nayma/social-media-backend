package com.socialmedia.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.app.dto.ApiResponse;
import com.socialmedia.app.dto.StoryDTO;
import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.StoryService;
import com.socialmedia.app.service.UserService;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Story> createStory(@AuthenticationPrincipal UserDetails user,
                                           @RequestParam("mediaFile") MultipartFile mediaFile,
                                           @RequestParam("caption") String caption) {
    	User userEntity = userRepository.findByUsername(user.getUsername())
    			.orElseThrow(() -> new RuntimeException("User not found"));
        Story createdStory = storyService.createStory(userEntity, mediaFile, caption);
        return new ResponseEntity<>(createdStory, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StoryDTO>> getUserStories(@PathVariable String userId) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<StoryDTO> stories = storyService.getStoriesOfUser(user);
        return new ResponseEntity<>(stories, HttpStatus.OK);
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long storyId) {
        storyService.deleteStory(storyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{storyId}/like")
    public ResponseEntity<Void> likeStory(@PathVariable Long storyId, @AuthenticationPrincipal UserDetails user) {
    	User userEntity = userRepository.findByUsername(user.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
        storyService.likeStory(storyId, userEntity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<Void> viewStory(@PathVariable Long storyId, @AuthenticationPrincipal UserDetails user) {
    	User userEntity = userRepository.findByUsername(user.getUsername())
    							.orElseThrow(() -> new RuntimeException("User not found"));
        storyService.viewStory(storyId, userEntity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{storyId}/likes")
    public ResponseEntity<ApiResponse<List<User>>> getStoryLikes(@PathVariable Long storyId) {
        List<User> users = storyService.getStoryLikes(storyId);
        return ResponseEntity.ok(ApiResponse.success("Fetched story likes successfully", users, HttpStatus.OK.value()));
    }

    @GetMapping("/{storyId}/views")
    public ResponseEntity<ApiResponse<List<User>>> getStoryViews(@PathVariable Long storyId) {
        List<User> users = storyService.getStoryViews(storyId);
        return ResponseEntity.ok(ApiResponse.success("Fetched story views successfully", users, HttpStatus.OK.value()));
    }

    @GetMapping("/feed")
    public ResponseEntity<com.socialmedia.app.dto.ApiResponse<?>> getFeedStories(@AuthenticationPrincipal UserDetails user) {
        List<StoryDTO> feedStories = storyService.getFeedStories(user.getUsername());
        return ResponseEntity.ok(
				com.socialmedia.app.dto.ApiResponse.success("Fetched feed stories successfully", feedStories, HttpStatus.OK.value())
		);
    }
    
    @GetMapping("/myStories")
    public ResponseEntity<ApiResponse<List<StoryDTO>>> getMyStories(@AuthenticationPrincipal UserDetails user) {
		User userEntity = userRepository.findByUsername(user.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		List<StoryDTO> stories = storyService.getStoriesOfUser(userEntity);
		return ResponseEntity.ok(
				com.socialmedia.app.dto.ApiResponse.success("Fetched my stories successfully", stories, HttpStatus.OK.value())
		);	}
}
