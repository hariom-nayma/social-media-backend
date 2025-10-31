package com.socialmedia.app.repository;

import com.socialmedia.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId);
    Page<Message> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(String conversationId);
    int countByConversationIdAndSeenFalseAndSenderIdNot(String conversationId, String senderId);
}