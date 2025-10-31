package com.socialmedia.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.app.dto.StoryDTO;
import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.User;

public interface StoryService {
	Story createStory(User user, MultipartFile mediaFile, String caption);
	List<StoryDTO> getStoriesOfUser(User user);
	void deleteStory(Long storyId);
	List<User> getStoryViews(Long storyId);
	List<User> getStoryLikes(Long storyId);
	void likeStory(Long storyId, User user);
	void viewStory(Long storyId, User user);
	List<StoryDTO> getFeedStories(String username);
}
