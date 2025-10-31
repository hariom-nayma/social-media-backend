package com.socialmedia.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

 @Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonIgnore
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content;
    private Instant sentAt = Instant.now();
    private boolean delivered = false;
    private boolean seen = false;
    private Instant seenAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReaction> reactions;    
}