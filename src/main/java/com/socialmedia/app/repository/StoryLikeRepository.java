package com.socialmedia.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.StoryLike;
import com.socialmedia.app.model.User;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
	List<StoryLike> findByStory(Story story);
	boolean existsByStoryAndUser(Story story, User user);
	boolean existsByStoryAndUserUsername(Story story, String username);
}
