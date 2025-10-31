package com.socialmedia.app.controller;

import com.socialmedia.app.constants.GlobalConstants;
import com.socialmedia.app.dto.*;
import com.socialmedia.app.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin( origins = GlobalConstants.FRONTEND_URL)
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Get all public posts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDTO>>> getAllPosts() {
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(
                ApiResponse.success("Fetched all posts successfully", posts, HttpStatus.OK.value())
        );
    }

    /**
     * Create a new post (with optional media)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @AuthenticationPrincipal UserDetails user,
            @RequestPart("content") String content,
            @RequestPart(value = "isPublic", required = false) Boolean isPublic,
            @RequestPart(value = "media", required = false) MultipartFile media) {

        String userId = user.getUsername();

        CreatePostRequest req = new CreatePostRequest();
        req.setContent(content);
        req.setIsPublic(isPublic);
        req.setMedia(media);

        PostDTO createdPost = postService.createPost(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", createdPost, HttpStatus.CREATED.value()));
    }

    /**
     * Get personalized feed for logged-in user
     */
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<FeedPostResponseDTO>>> getFeed(@AuthenticationPrincipal UserDetails user) {
        String userId = user.getUsername();
        List<FeedPostResponseDTO> feed = postService.getFeed(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched user feed successfully", feed, HttpStatus.OK.value())
        );
    }

    /**
     * Get posts of a specific user
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<List<FeedPostResponseDTO>>> getUserPosts(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {

        String viewerId = user.getUsername();
        List<FeedPostResponseDTO> posts = postService.getUserPosts(id, viewerId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched user's posts successfully", posts, HttpStatus.OK.value())
        );
    }

    /**
     * Get a single post by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedPostResponseDTO>> getPostById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {

        String viewerId = user.getUsername();
        FeedPostResponseDTO post = postService.getPostByIdWithFeedResponse(id, viewerId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched post successfully", post, HttpStatus.OK.value())
        );
    }

    /**
     * Like or unlike a post (toggle)
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<String>> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
    	System.out.println("Toggling like for post ID: " + id + " by user: " + user.getUsername());
        String username = user.getUsername();
        String message = postService.toggleLike(username, id);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }
    
    
//    
//    @PostMapping("/{id}/dislike")
//    public ResponseEntity<ApiResponse<String>> toggleDislike(
//			@PathVariable String id,
//			@AuthenticationPrincipal UserDetails user) {
//		System.out.println("Toggling dislike for post ID: " + id + " by user: " + user.getUsername());
//		String userId = user.getUsername();
//		String message = postService.toggleDislike(userId, id);
//		return ResponseEntity.ok(
//				ApiResponse.success(message, null, HttpStatus.OK.value())
//		);
//	}

    /**
     * Add a comment to a post
     */
    @PostMapping("/{id}/comment")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CreateCommentRequest req) {

        String userId = user.getUsername();
        CommentDTO comment = postService.addComment(userId, id, req);
        return ResponseEntity.ok(
                ApiResponse.success("Comment added successfully", comment, HttpStatus.OK.value())
        );
    }

    /**
     * Like or unlike a comment (toggle)
     */
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<ApiResponse<String>> toggleCommentLike(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails user) {
        String userId = user.getUsername();
        String message = postService.toggleCommentLike(userId, commentId);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Get comments for a post with pagination and sorting.
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails user) {
        String userId = user.getUsername();
        List<CommentDTO> comments = postService.getCommentsByPost(postId, userId, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Comments fetched successfully", comments, HttpStatus.OK.value())
        );
    }

    /**
     * Get child comments for a comment.
     */
    @GetMapping("/comment/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails user) {
        String userId = user.getUsername();
        List<CommentDTO> replies = postService.getCommentReplies(commentId, userId, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Replies fetched successfully", replies, HttpStatus.OK.value())
        );
    }

    /**
     * Save or unsave a post (toggle)
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<String>> toggleSavePost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        String message = postService.toggleSavePost(username, id);
        return ResponseEntity.ok(
                ApiResponse.success(message, null, HttpStatus.OK.value())
        );
    }

    /**
     * Get saved posts for the logged-in user with pagination
     */
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<FeedPostResponseDTO>>> getSavedPosts(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = user.getUsername();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        List<FeedPostResponseDTO> savedPosts = postService.getSavedPosts(username, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Saved posts fetched successfully", savedPosts, HttpStatus.OK.value())
        );
    }
}
