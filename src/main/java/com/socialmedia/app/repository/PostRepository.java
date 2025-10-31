package com.socialmedia.app.repository;

import com.socialmedia.app.model.Post;
import com.socialmedia.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByUser(User user);
    List<Post> findByIsPublicTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Post p WHERE p.user IN :users ORDER BY p.createdAt DESC")
    List<Post> findByUserInOrderByCreatedAtDesc(@Param("users") Set<User> users);
}

