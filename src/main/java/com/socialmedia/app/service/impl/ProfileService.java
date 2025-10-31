package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.UserProfileDTO;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Cacheable(value = "userProfileCache", key = "#userId")
    public UserProfileDTO getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserProfileDTO(user.getId(), user.getUsername(), user.getProfileImageUrl(),user.getFirstName(), user.getLastName(), user.getBio(), user.isPrivate(), user.getFollowers().size(), user.getFollowing().size());
    }
}
