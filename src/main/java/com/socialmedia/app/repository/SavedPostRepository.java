package com.socialmedia.app.repository;

import com.socialmedia.app.model.Post;
import com.socialmedia.app.model.SavedPost;
import com.socialmedia.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    Page<SavedPost> findByUser(User user, Pageable pageable);
    Optional<SavedPost> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    boolean existsByUserUsernameAndPost(String username, Post post);
}