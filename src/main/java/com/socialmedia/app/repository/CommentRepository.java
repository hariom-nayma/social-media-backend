package com.socialmedia.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.Comment;
import com.socialmedia.app.model.Post;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByPost(Post post);
}
