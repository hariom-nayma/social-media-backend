package com.socialmedia.app.repository;

import com.socialmedia.app.model.ChatMessage;
import com.socialmedia.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    // Fetch conversation between two users (ordered by time)
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(User user1, User user2);
    List<ChatMessage> findTop50ByReceiverOrderByCreatedAtDesc(User receiver);
}
