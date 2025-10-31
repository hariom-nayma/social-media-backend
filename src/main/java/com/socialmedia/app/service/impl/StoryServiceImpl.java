package com.socialmedia.app.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.StoryLike;
import com.socialmedia.app.model.StoryView;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.StoryLikeRepository;
import com.socialmedia.app.repository.StoryRepository;
import com.socialmedia.app.repository.StoryViewRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.StoryService;
import com.socialmedia.app.dto.StoryDTO;
import org.modelmapper.ModelMapper;

@Service
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryViewRepository storyViewRepository;

    @Autowired
    private StoryLikeRepository storyLikeRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Story createStory(User user, MultipartFile mediaFile, String caption) {
        Story story = new Story();
        story.setUser(user);
        story.setCaption(caption);
        story.setCreatedAt(LocalDateTime.now());

        if (mediaFile != null && !mediaFile.isEmpty()) {
            String mediaUrl = cloudinaryService.uploadImage(mediaFile);
            story.setContentUrl(mediaUrl);
        } else {
            throw new IllegalArgumentException("Media file cannot be empty for a story.");
        }
        return storyRepository.save(story);
    }

    @Override
    public List<StoryDTO> getStoriesOfUser(User user) {
    			return storyRepository.findByUser(user).stream()
				.map(story -> convertToStoryDTO(story, user.getUsername()))
				.collect(Collectors.toList());
    }

    @Override
    public void deleteStory(Long storyId) {
        storyRepository.deleteById(storyId);
    }

    @Override
    public List<User> getStoryViews(Long storyId) {
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        List<StoryView> storyViews = storyViewRepository.findByStory(story);
        return storyViews.stream().map(StoryView::getUser).collect(Collectors.toList());
    }

    @Override
    public List<User> getStoryLikes(Long storyId) {
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        List<StoryLike> storyLikes = storyLikeRepository.findByStory(story);
        return storyLikes.stream().map(StoryLike::getUser).collect(Collectors.toList());
    }

    @Override
    public void likeStory(Long storyId, User user) {
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        if (!storyLikeRepository.existsByStoryAndUser(story, user)) {
            StoryLike storyLike = new StoryLike();
            storyLike.setStory(story);
            storyLike.setUser(user);
            storyLike.setCreatedAt(LocalDateTime.now());
            storyLikeRepository.save(storyLike);
        }
    }

    @Override
    public void viewStory(Long storyId, User user) {
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        if (!storyViewRepository.existsByStoryAndUser(story, user)) {
            StoryView storyView = new StoryView();
            storyView.setStory(story);
            storyView.setUser(user);
            storyView.setCreatedAt(LocalDateTime.now());
            storyViewRepository.save(storyView);
        }
    }

    @Override
    public List<StoryDTO> getFeedStories(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> followingUsers = user.getFollowing().stream().collect(Collectors.toList());
//        followingUsers.add(user); // Include current user's stories in the feed

        return storyRepository.findByUserInOrderByCreatedAtDesc(followingUsers).stream()
                .map(story -> convertToStoryDTO(story, username))
                .sorted(Comparator.comparing(StoryDTO::isViewedByMe))
                .collect(Collectors.toList());
    }


    private StoryDTO convertToStoryDTO(Story story, String viewerUsername) {
        StoryDTO storyDTO = modelMapper.map(story, StoryDTO.class);
        storyDTO.setUserId(story.getUser().getId());
        storyDTO.setUsername(story.getUser().getUsername());
        storyDTO.setFirstName(story.getUser().getFirstName());
        storyDTO.setLastName(story.getUser().getLastName());
        storyDTO.setProfileImageUrl(story.getUser().getProfileImageUrl());
        storyDTO.setLikedByme(storyLikeRepository.existsByStoryAndUserUsername(story, viewerUsername));
        storyDTO.setViewedByMe(storyViewRepository.existsByStoryAndUserUsername(story, viewerUsername));
        return storyDTO;
    }
}

