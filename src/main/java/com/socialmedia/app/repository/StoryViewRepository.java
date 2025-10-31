package com.socialmedia.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.StoryView;
import com.socialmedia.app.model.User;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
	List<StoryView> findByStory(Story story);
	boolean existsByStoryAndUser(Story story, User user);
	boolean existsByStoryAndUserUsername(Story story, String username);
}
