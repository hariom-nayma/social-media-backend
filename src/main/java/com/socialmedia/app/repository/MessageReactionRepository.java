package com.socialmedia.app.repository;

import com.socialmedia.app.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {
    Optional<MessageReaction> findByMessageIdAndUserId(String messageId, String userId);
}
