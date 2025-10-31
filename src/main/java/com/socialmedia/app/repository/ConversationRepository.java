package com.socialmedia.app.repository;

import com.socialmedia.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    List<Conversation> findByUser1IdOrUser2Id(String user1Id, String user2Id);
}