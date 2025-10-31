package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.UpdateProfileRequest;
import com.socialmedia.app.dto.UserDTO;
import com.socialmedia.app.enums.NotificationType;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.NotificationService;
import com.socialmedia.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;

import com.socialmedia.app.model.FollowRequest;
import com.socialmedia.app.repository.FollowRequestRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRequestRepository followRequestRepository;
    private final ModelMapper mapper;
    private final NotificationService notificationService;

    public UserServiceImpl(UserRepository userRepository, FollowRequestRepository followRequestRepository, ModelMapper mapper, NotificationServiceImpl notificationService) {
        this.userRepository = userRepository;
        this.followRequestRepository = followRequestRepository;
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    @Override
    public UserDTO getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDTO(user);
    }

    @Override
//    @CacheEvict(value = "users", allEntries = true)
    public UserDTO updateMyProfile(String username, UpdateProfileRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName().trim());
        if (req.getLastName() != null) user.setLastName(req.getLastName().trim());
        if (req.getBio() != null) user.setBio(req.getBio().trim());
        if (req.getIsPrivate() != null) user.setPrivate(req.getIsPrivate());
        if (req.getProfileImageUrl() != null) user.setProfileImageUrl(req.getProfileImageUrl());

        userRepository.save(user);
        return toDTO(user);
    }

    @Override
    public UserDTO getUserProfile(String targetId, String viewerId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));

        if (target.isPrivate() && !target.getFollowers().contains(viewer) && !target.equals(viewer)) {
            throw new SecurityException("This profile is private");
        }
        return toDTO(target);
    }

    @Override
    public String followUser(String followerUsername, String targetId) {
    	
    	User follower = userRepository.findByUsername(followerUsername)
				.orElseThrow(() -> new EntityNotFoundException("Follower not found"));
    			String followerId = follower.getId();
        if (followerId.equals(targetId)) return "You cannot follow yourself";
        
        

//        User follower = userRepository.findById(followerId)
//                .orElseThrow(() -> new EntityNotFoundException("Follower not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        if (target.getFollowers().contains(follower)) return "Already following";

        if (target.isPrivate()) {
            if (followRequestRepository.findByFollowerAndTarget(follower, target).isPresent()) {
            	//delete request if already sent
            	followRequestRepository.deleteByFollowerAndTarget(follower, target);
                return "Follow deleted";
            }
            FollowRequest followRequest = new FollowRequest(follower, target);
            followRequestRepository.save(followRequest);
            notificationService.pushNotification(target.getUsername(),
                    NotificationType.FOLLOW_REQUEST,
                    follower.getUsername() + " sent you a follow request",
                    "{\"followerId\":\"" + follower.getId() + "\"}");
            return "Follow request sent";
        } else {
            target.getFollowers().add(follower);
            userRepository.save(target);
            notificationService.pushNotification(target.getUsername(),
                    NotificationType.FOLLOW,
                    follower.getUsername() + " started following you",
                    "{\"followerId\":\"" + follower.getId() + "\"}");
            return "Followed " + target.getUsername();
        }
    }

    @Override
    public String unfollowUser(String followerUsername, String targetId) {
    	User follower = userRepository.findByUsername(followerUsername)
    							.orElseThrow(() -> new EntityNotFoundException("Follower not found"));
    			String followerId = follower.getId();
//        User follower = userRepository.findById(followerId)
//                .orElseThrow(() -> new EntityNotFoundException("Follower not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        followRequestRepository.findByFollowerAndTarget(follower, target).ifPresent(followRequestRepository::delete);

        if (target.getFollowers().remove(follower)) {
            userRepository.save(target);
            return "Unfollowed " + target.getUsername();
        } else {
            return "Not following";
        }
    }

    @Override
    public List<UserDTO> getFollowers(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getFollowers().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getFollowing(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getFollowing().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = mapper.map(user, UserDTO.class);
        dto.setFollowersCount(user.getFollowers().size());
        dto.setFollowingCount(user.getFollowing().size());
        return dto;
    }
    
    @Override
    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName", "lastName"));
    }

    @Override
    public String acceptFollowRequest(String userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FollowRequest followRequest = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Follow request not found"));

        if (!followRequest.getTarget().equals(user)) {
            throw new SecurityException("You are not authorized to accept this follow request");
        }

        followRequest.setAccepted(true);
        followRequestRepository.save(followRequest);

        User follower = followRequest.getFollower();
        user.getFollowers().add(follower);
        userRepository.save(user);

        notificationService.pushNotification(follower.getUsername(),
                NotificationType.FOLLOW_ACCEPTED,
                user.getUsername() + " accepted your follow request",
                "{\"userId\":\"" + user.getId() + "\"}");

        return "Follow request accepted";
    }

    @Override
    public String declineFollowRequest(String userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FollowRequest followRequest = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Follow request not found"));

        if (!followRequest.getTarget().equals(user)) {
            throw new SecurityException("You are not authorized to decline this follow request");
        }

        followRequestRepository.delete(followRequest);

        return "Follow request declined";
    }

    @Override
    public List<com.socialmedia.app.dto.FollowRequestDTO> getPendingFollowRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<FollowRequest> followRequests = followRequestRepository.findByTargetAndAccepted(user, false);
        return followRequests.stream()
                .map(followRequest -> com.socialmedia.app.dto.FollowRequestDTO.builder()
                        .id(followRequest.getId())
                        .follower(toDTO(followRequest.getFollower()))
                        .target(toDTO(followRequest.getTarget()))
                        .accepted(followRequest.isAccepted())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<com.socialmedia.app.dto.FriendSuggestionDTO> getFriendSuggestions(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        // Get all users
        List<User> allUsers = userRepository.findAll();

        // Get users currently followed by the current user
        Set<User> currentUserFollowing = currentUser.getFollowing();

        // Filter out current user, users already followed, and users who are private and not followed
        List<User> candidateUsers = allUsers.stream()
                .filter(user -> !user.equals(currentUser)) // Exclude current user
                .filter(user -> !currentUserFollowing.contains(user)) // Exclude users already followed
                .collect(Collectors.toList());

        List<com.socialmedia.app.dto.FriendSuggestionDTO> suggestions = candidateUsers.stream()
                .map(candidate -> {
                    long mutualCount = currentUser.getFollowers().stream()
                            .filter(candidate.getFollowers()::contains)
                            .count();

                    boolean isFollowMe = candidate.getFollowing().contains(currentUser);
                    boolean isPrivate = candidate.isPrivate();
                    boolean isRequested = followRequestRepository.findByFollowerAndTarget(currentUser, candidate).isPresent();

                    return com.socialmedia.app.dto.FriendSuggestionDTO.builder()
                            .userId(java.util.UUID.fromString(candidate.getId()))
                            .firstName(candidate.getFirstName())
                            .lastName(candidate.getLastName())
                            .username(candidate.getUsername())
                            .profileUrl(candidate.getProfileImageUrl())
                            .isFollowMe(isFollowMe)
                            .isPrivate(isPrivate)
                            .isRequested(isRequested)
                            .mutualCount(mutualCount)
                            .build();
                })
                .sorted((s1, s2) -> {
                    int mutualCompare = Long.compare(s2.getMutualCount(), s1.getMutualCount());
                    if (mutualCompare != 0) {
                        return mutualCompare;
                    }
                    // If mutual counts are equal, prioritize users who follow the current user
                    return Boolean.compare(s2.isFollowMe(), s1.isFollowMe());
                })
                .collect(Collectors.toList());

        return suggestions;
    }

    private com.socialmedia.app.dto.UserRelationshipDTO toUserRelationshipDTO(User targetUser, User viewerUser) {
        long mutualCount = viewerUser.getFollowers().stream()
                .filter(targetUser.getFollowers()::contains)
                .count();

        boolean isFollowing = viewerUser.getFollowing().contains(targetUser);
        boolean isRequested = targetUser.isPrivate() && followRequestRepository.findByFollowerAndTarget(viewerUser, targetUser).isPresent();

        return com.socialmedia.app.dto.UserRelationshipDTO.builder()
                .userId(java.util.UUID.fromString(targetUser.getId()))
                .firstName(targetUser.getFirstName())
                .lastName(targetUser.getLastName())
                .username(targetUser.getUsername())
                .profileImageUrl(targetUser.getProfileImageUrl())
                .isPrivate(targetUser.isPrivate())
                .isFollowing(isFollowing)
                .isRequested(isRequested)
                .mutualCount(mutualCount)
                .build();
    }

    @Override
    public List<com.socialmedia.app.dto.UserRelationshipDTO> getFollowersWithDetails(String targetUserId, String viewerId) {
        User targetUser = userRepository.findByUsername(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));
        User viewerUser = userRepository.findByUsername(viewerId)
                .orElseThrow(() -> new EntityNotFoundException("Viewer user not found"));

        return targetUser.getFollowers().stream()
                .map(follower -> toUserRelationshipDTO(follower, viewerUser))
                .sorted((u1, u2) -> Long.compare(u2.getMutualCount(), u1.getMutualCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<com.socialmedia.app.dto.UserRelationshipDTO> getFollowingWithDetails(String targetUserId, String viewerId) {
        User targetUser = userRepository.findByUsername(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));
        User viewerUser = userRepository.findByUsername(viewerId)
                .orElseThrow(() -> new EntityNotFoundException("Viewer user not found"));

        return targetUser.getFollowing().stream()
                .map(following -> toUserRelationshipDTO(following, viewerUser))
                .sorted((u1, u2) -> Long.compare(u2.getMutualCount(), u1.getMutualCount()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

	public void markPhoneVerified(String phone) {
//		userRepository.findByPhoneNumber(phone).ifPresent(user -> {
//			user.setPhoneVerified(true);
//			userRepository.save(user);
//		});
		
	}
}
