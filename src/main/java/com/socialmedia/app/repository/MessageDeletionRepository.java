package com.socialmedia.app.repository;

import com.socialmedia.app.model.MessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageDeletionRepository extends JpaRepository<MessageDeletion, String> {
    List<MessageDeletion> findByUserIdAndMessageConversationId(String userId, String conversationId);
}
