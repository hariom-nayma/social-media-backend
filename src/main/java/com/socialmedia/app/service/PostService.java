package com.socialmedia.app.service;

import com.socialmedia.app.dto.CommentDTO;
import com.socialmedia.app.dto.CreateCommentRequest;
import com.socialmedia.app.dto.CreatePostRequest;
import com.socialmedia.app.dto.FeedPostResponseDTO;
import com.socialmedia.app.dto.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PostService {
    PostDTO createPost(String username, CreatePostRequest req);
    List<FeedPostResponseDTO> getUserPosts(String targetUsername, String viewerUsername);
    List<FeedPostResponseDTO> getFeed(String viewerId);
    String toggleLike(String userId, String postId);
	PostDTO getPostById(String postId, String viewerId);
	List<PostDTO> getAllPosts();
	    CommentDTO addComment(String userId, String postId, CreateCommentRequest req);
	    String toggleCommentLike(String userId, String commentId);
	        FeedPostResponseDTO getPostByIdWithFeedResponse(String postId, String viewerId);
	            List<CommentDTO> getCommentsByPost(String postId, String userId, int page, int size);
	            List<CommentDTO> getCommentReplies(String commentId, String userId, int page, int size);
				String toggleUnlike(String userId, String postId);
    String toggleSavePost(String username, String postId);
    List<FeedPostResponseDTO> getSavedPosts(String username, Pageable pageable);
        }
