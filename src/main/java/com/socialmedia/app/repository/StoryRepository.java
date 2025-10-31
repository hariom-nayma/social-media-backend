package com.socialmedia.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.Story;
import com.socialmedia.app.model.User;

public interface StoryRepository extends JpaRepository<Story, Long> {
	List<Story> findByUser(User user);

	List<Story> findByUserInOrderByCreatedAtDesc(List<User> followingUsers);
}
