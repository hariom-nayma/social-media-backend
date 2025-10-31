package com.socialmedia.app.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialmedia.app.dto.CommentDTO;
import com.socialmedia.app.dto.CreateCommentRequest;
import com.socialmedia.app.dto.CreatePostRequest;
import com.socialmedia.app.dto.FeedPostResponseDTO;
import com.socialmedia.app.dto.PostDTO;
import com.socialmedia.app.enums.NotificationType;
import com.socialmedia.app.model.Comment;
import com.socialmedia.app.model.Post;
import com.socialmedia.app.model.SavedPost;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.CommentRepository;
import com.socialmedia.app.repository.PostRepository;
import com.socialmedia.app.repository.SavedPostRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.NotificationService;
import com.socialmedia.app.service.PostService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper mapper;
    private final NotificationService notificationService;
    private final SavedPostRepository savedPostRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository,
                           CommentRepository commentRepository, CloudinaryService cloudinaryService,
                           ModelMapper mapper, NotificationServiceImpl notificationService, SavedPostRepository savedPostRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.savedPostRepository = savedPostRepository;
    }

    @Override
//    @CacheEvict(value = "posts", allEntries = true)
    public PostDTO createPost(String username, CreatePostRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setContent(req.getContent());
        post.setPublic(req.getIsPublic() != null ? req.getIsPublic() : true);

        if (req.getMedia() != null && !req.getMedia().isEmpty()) {
            String url = cloudinaryService.uploadImage(req.getMedia());
            post.setMediaUrl(url);
        }

        Post saved = postRepository.save(post);
        return toDTO(saved, null);
    }

    @Override
    public List<FeedPostResponseDTO> getUserPosts(String targetUsername, String viewerUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User viewer = userRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));

        boolean canViewPrivate = !target.isPrivate() || target.equals(viewer)
                || target.getFollowers().contains(viewer);

        List<Post> posts = postRepository.findByUser(target);
        return posts.stream()
                .filter(p -> p.isPublic() || canViewPrivate)
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(post -> toFeedPostResponseDTO(post, viewerUsername))
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedPostResponseDTO> getFeed(String viewerUsername) {
        User viewer = userRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));

        Set<User> followers = viewer.getFollowing(); // Get users who follow the viewer

        // Get posts from users who follow the viewer
        List<Post> posts = postRepository.findByUserInOrderByCreatedAtDesc(followers);

        return posts.stream()
                .filter(post -> post.isPublic() || viewer.getFollowing().contains(post.getUser())) // Include public posts or private posts if viewer follows the author
                .map(post -> toFeedPostResponseDTO(post, viewerUsername))
                .collect(Collectors.toList());
    }

    private FeedPostResponseDTO toFeedPostResponseDTO(Post post, String viewerUsername) {
        FeedPostResponseDTO dto = mapper.map(post, FeedPostResponseDTO.class);
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getUsername());
        dto.setFirstName(post.getUser().getFirstName());
        dto.setLastName(post.getUser().getLastName());
        dto.setProfileImage(post.getUser().getProfileImageUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(post.getLikedBy().size());
        dto.setCommentsCount(post.getComments().size());
        dto.setLikedByCurrentUser(post.getLikedBy().stream().anyMatch(user -> user.getUsername().equals(viewerUsername)));
        dto.setSavedByMe(savedPostRepository.existsByUserUsernameAndPost(viewerUsername, post));
//        dto.setComments(post.getComments().stream()
//                .filter(c -> c.getParentComment() == null)
//                .map(this::toCommentDTO).collect(Collectors.toList()));
        return dto;
    }

    @Override
//    @CacheEvict(value = "posts", allEntries = true)
    public String toggleLike(String username, String postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
            postRepository.save(post);
            return "Unliked post";
        } else {
            post.getLikedBy().add(user);
            postRepository.save(post);
            notificationService.pushNotification(post.getUser().getUsername(),
                NotificationType.LIKE,
                user.getUsername() + " liked your post",
                "{\"postId\":\"" + post.getId() + "\"}");
            return "Liked post";
        }
    }
    
        @Override
    
        public String toggleUnlike(String userId, String postId) {
    
    		User user = userRepository.findById(userId)
    
    				.orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    		Post post = postRepository.findById(postId)
    
    				.orElseThrow(() -> new EntityNotFoundException("Post not found"));
    
    
    
    		if (post.getLikedBy().contains(user)) {
    
    			post.getLikedBy().remove(user);
    
    			postRepository.save(post);
    
    			return "Unliked post";
    
    		} else {
    
    			return "Post not liked yet";
    
    		}
    
    	}
    
    
    
        @Override
    
        public CommentDTO addComment(String username, String postId, CreateCommentRequest req) {
    
            System.out.println("Adding comment for user {} to post {}"+ username+"  " +  postId);
    
            User user = userRepository.findByUsername(username)
    
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
            Post post = postRepository.findById(postId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    
    
    
            Comment comment = new Comment();
    
            comment.setText(req.getText());
    
            comment.setUser(user);
    
            comment.setPost(post);
    
    
    
            if (req.getParentCommentId() != null) {
    
                Comment parentComment = commentRepository.findById(req.getParentCommentId())
    
                        .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
    
                comment.setParentComment(parentComment);
    
            }
    
    
    
            commentRepository.save(comment);
    
            notificationService.pushNotification(post.getUser().getUsername(),
    
            	    NotificationType.COMMENT,
    
            	    user.getUsername() + " commented: " + req.getText(),
    
            	    "{\"postId\":\"" + post.getId() + "\"}");
    
    
    
            return toCommentDTO(comment, username);
    
        }
    
    
    
        private PostDTO toDTO(Post post, String viewerUsername) {
    
            PostDTO dto = mapper.map(post, PostDTO.class);
    
            dto.setUserId(post.getUser().getId());
    
            dto.setUsername(post.getUser().getUsername());
    
            dto.setLikeCount(post.getLikedBy().size());
    
            dto.setComments(post.getComments().stream()
    
                    .filter(c -> c.getParentComment() == null)
    
                    .map(comment -> toCommentDTO(comment, viewerUsername)).collect(Collectors.toList()));
    
            return dto;
    
        }
    
    
    
        private CommentDTO toCommentDTO(Comment c, String viewerUsername) {
    
            CommentDTO dto = mapper.map(c, CommentDTO.class);
    
            dto.setUserId(c.getUser().getId());
    
            dto.setUsername(c.getUser().getUsername());
    
            dto.setFirstName(c.getUser().getFirstName());
    
            dto.setLastName(c.getUser().getLastName());
    
            dto.setUserProfileImage(c.getUser().getProfileImageUrl());
    
            dto.setLikesCount(c.getLikes().size());
    
            dto.setLikedByCurrentUser(viewerUsername != null && c.getLikes().stream().anyMatch(user -> user.getUsername().equals(viewerUsername)));
    
            if (c.getParentComment() != null) {
    
                dto.setParentCommentId(c.getParentComment().getId());
    
            }
    
            dto.setReplies(c.getReplies().stream().map(reply -> toCommentDTO(reply, viewerUsername)).collect(Collectors.toSet()));
    
            return dto;
    
        }
    
        
    
        @Override
    
        @Cacheable(value = "posts")
    
        public List<PostDTO> getAllPosts() {
    
            return postRepository.findAll().stream().map(post -> toDTO(post, null)).collect(Collectors.toList());
    
        }
    
    
    
        @Override
    
        public PostDTO getPostById(String postId, String viewerUsername) {
    
            Post post = postRepository.findById(postId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    
            User viewer = userRepository.findByUsername(viewerUsername)
    
                    .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));
    
    
    
            if (!post.isPublic() && !post.getUser().equals(viewer) && !post.getUser().getFollowers().contains(viewer)) {
    
                throw new SecurityException("You are not allowed to see this post");
    
            }
    
    
    
            return toDTO(post, viewerUsername);
    
        }
    
    
    
        @Override
    
        public String toggleCommentLike(String username, String commentId) {
    
            User user = userRepository.findByUsername(username)
    
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
            Comment comment = commentRepository.findById(commentId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    
            if (comment.getLikes().contains(user)) {
    
                comment.getLikes().remove(user);
    
                commentRepository.save(comment);
    
                return "Unliked comment";
    
            } else {
    
                comment.getLikes().add(user);
    
                commentRepository.save(comment);
    
                return "Liked comment";
    
            }
    
        }
    
    
    
        @Override
    
        public FeedPostResponseDTO getPostByIdWithFeedResponse(String postId, String viewerUsername) {
    
            Post post = postRepository.findById(postId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    
            User viewer = userRepository.findByUsername(viewerUsername)
    
                    .orElseThrow(() -> new EntityNotFoundException("Viewer not found"));
    
    
    
            if (!post.isPublic() && !post.getUser().equals(viewer) && !post.getUser().getFollowers().contains(viewer)) {
    
                throw new SecurityException("You are not allowed to see this post");
    
            }
    
    
    
            return toFeedPostResponseDTO(post, viewerUsername);
    
        }
    
    
    
        @Override
    
        public List<CommentDTO> getCommentsByPost(String postId, String username, int page, int size) {
    
            Post post = postRepository.findById(postId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    
    
    
            List<Comment> comments = post.getComments().stream()
    
                    .filter(c -> c.getParentComment() == null)
    
                    .sorted(Comparator.comparing((Comment c) -> c.getUser().getUsername().equals(username)).reversed()
    
                            .thenComparing(c -> c.getLikes().size(), Comparator.reverseOrder()))
    
                    .collect(Collectors.toList());
    
    
    
            int start = page * size;
    
            int end = Math.min(start + size, comments.size());
    
    
    
            if (start > end) {
    
                return Collections.emptyList();
    
            }
    
    
    
            return comments.subList(start, end).stream()
    
                    .map(comment -> toCommentDTO(comment, username))
    
                    .collect(Collectors.toList());
    
        }
    
    
    
        @Override
    
        public List<CommentDTO> getCommentReplies(String commentId, String username, int page, int size) {
    
            Comment parentComment = commentRepository.findById(commentId)
    
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
    
    
    
            List<Comment> replies = parentComment.getReplies().stream()
    
                    .sorted(Comparator.comparing((Comment c) -> c.getUser().getUsername().equals(username)).reversed()
    
                            .thenComparing(c -> c.getLikes().size(), Comparator.reverseOrder()))
    
                    .collect(Collectors.toList());
    
    
    
            int start = page * size;
    
            int end = Math.min(start + size, replies.size());
    
    
    
            if (start > end) {
    
                return Collections.emptyList();
    
            }
    
    
    
            return replies.subList(start, end).stream()
    
                    .map(reply -> toCommentDTO(reply, username))
    
                    .collect(Collectors.toList());
    
        }

        @Override
        public String toggleSavePost(String username, String postId) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));

            Optional<SavedPost> savedPostOptional = savedPostRepository.findByUserAndPost(user, post);

            if (savedPostOptional.isPresent()) {
                savedPostRepository.delete(savedPostOptional.get());
                return "Post unsaved";
            } else {
                SavedPost savedPost = new SavedPost();
                savedPost.setUser(user);
                savedPost.setPost(post);
                savedPostRepository.save(savedPost);
                return "Post saved";
            }
        }

        @Override
        public List<FeedPostResponseDTO> getSavedPosts(String username, org.springframework.data.domain.Pageable pageable) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            return savedPostRepository.findByUser(user, pageable).stream()
                    .map(savedPost -> toFeedPostResponseDTO(savedPost.getPost(), username))
                    .collect(Collectors.toList());
        }
    }
