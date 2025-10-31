package com.socialmedia.app.service.impl;

import java.util.Collections;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.socialmedia.app.dto.FeedPostResponseDTO;
import com.socialmedia.app.dto.UserProfileDTO;
import com.socialmedia.app.dto.UserProfileWithPostsDTO;
import com.socialmedia.app.model.Conversation;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.ConversationRepository;
import com.socialmedia.app.repository.FollowRequestRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.PostService;
import com.socialmedia.app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProfileServiceImpl implements com.socialmedia.app.service.ProfileService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PostService postService;
    private final ModelMapper mapper;
    private final FollowRequestRepository followRequestRepository;
    private final ConversationRepository conversationRepository;

    public ProfileServiceImpl(UserRepository userRepository, UserService userService, PostService postService, ModelMapper mapper, FollowRequestRepository followRequestRepository, ConversationRepository conversationRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.postService = postService;
        this.mapper = mapper;
        this.followRequestRepository = followRequestRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public UserProfileWithPostsDTO getUserProfileWithPosts(String targetUsername, String viewerUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User viewer = userRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));

        UserProfileWithPostsDTO profile = mapper.map(target, UserProfileWithPostsDTO.class);
        profile.setFollowersCount(target.getFollowers().size());
        profile.setFollowingCount(target.getFollowing().size());
        profile.setIsPrivate(target.isPrivate());

        // Populate isFollowing and isRequested
        profile.setFollowing(target.getFollowers().contains(viewer));
        profile.setRequested(target.isPrivate() && followRequestRepository.findByFollowerAndTarget(viewer, target).isPresent());
        
        boolean canViewPosts = !target.isPrivate() || target.equals(viewer) || target.getFollowers().contains(viewer);

        if (canViewPosts) {
            List<FeedPostResponseDTO> posts = postService.getUserPosts(targetUsername, viewerUsername);
            profile.setPosts(posts);
        } else {
            profile.setPosts(Collections.emptyList());
        }

        return profile;
    }

    @Override
    public UserProfileWithPostsDTO getUserProfileWithPostsByUsername(String username, String viewerUsername) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return getUserProfileWithPosts(username, viewerUsername);
    }

	@Override
	public UserProfileWithPostsDTO getUserProfileWithPostsByConversationId(String conversationId, String viewerId) {
		User viewer = userRepository.findByUsername(viewerId)
				.orElseThrow(() -> new EntityNotFoundException("Viewer not found"));
		
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
		
		User target = conversation.getUser2().equals(viewer) ? conversation.getUser1() : conversation.getUser2();
		return getUserProfileWithPosts(target.getUsername(), viewerId);
		
		
	}
    
    
}
