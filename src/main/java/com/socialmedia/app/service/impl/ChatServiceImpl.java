package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.ChatMessageDto;
import com.socialmedia.app.dto.ReactionDTO;
import com.socialmedia.app.model.*;
import com.socialmedia.app.repository.*;
import com.socialmedia.app.service.ChatService;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.socialmedia.app.dto.ConversationListDTO;
import com.socialmedia.app.dto.MessageResponseDTO;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final MessageReactionRepository reactionRepo;


    private final MessageDeletionRepository messageDeletionRepository;

    public ChatServiceImpl(ConversationRepository c, MessageRepository m, UserRepository u, MessageReactionRepository r, MessageDeletionRepository mdr) {
        this.conversationRepo = c;
        this.messageRepo = m;
        this.userRepo = u;
        this.reactionRepo = r;
        this.messageDeletionRepository = mdr;
    }

    @Override
    public MessageResponseDTO sendMessage(ChatMessageDto dto) {
        var sender = userRepo.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        var recipient = userRepo.findById(dto.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        var convo = conversationRepo.findByUser1IdAndUser2Id(sender.getId(), recipient.getId())
                .or(() -> conversationRepo.findByUser1IdAndUser2Id(recipient.getId(), sender.getId()))
                .orElseGet(() -> {
                    var c = new Conversation();
                    c.setUser1(sender);
                    c.setUser2(recipient);
                    return conversationRepo.save(c);
                });

        var message = new Message();
        message.setConversation(convo);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setSentAt(Instant.now());
        message.setDelivered(true);

        Message savedMessage = messageRepo.save(message);

        // Explicitly initialize reactions collection
        savedMessage.getReactions().size(); // Forces initialization

        return new MessageResponseDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getSender().getUsername(),
                savedMessage.getSender().getFirstName(),
                savedMessage.getSender().getLastName(),
                savedMessage.getSender().getProfileImageUrl(),
                savedMessage.getContent(),
                savedMessage.getSentAt(),
                savedMessage.isDelivered(),
                savedMessage.isSeen(),
                savedMessage.getSeenAt(),
                savedMessage.getReactions().stream().map(reaction -> new ReactionDTO(reaction.getMessage().getId(), reaction.getReaction(), reaction.getUser().getId())).collect(Collectors.toList())
        );
    }

    @Override
    public MessageReaction reactToMessage(ReactionDTO dto) {
        var user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var message = messageRepo.findById(dto.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        var reaction = reactionRepo.findByMessageIdAndUserId(message.getId(), user.getId())
                .orElse(new MessageReaction());

        reaction.setMessage(message);
        reaction.setUser(user);
        reaction.setReaction(dto.getReaction());

        return reactionRepo.save(reaction);
    }

    @Override
    public Page<MessageResponseDTO> getMessages(String conversationId, int page, int size, String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messages = messageRepo.findByConversationIdOrderBySentAtDesc(conversationId, pageable);

        List<String> deletedMessageIds = messageDeletionRepository.findByUserIdAndMessageConversationId(user.getId(), conversationId)
                .stream()
                .map(deletion -> deletion.getMessage().getId())
                .toList();

        return messages.map(message -> {
            if (deletedMessageIds.contains(message.getId())) {
                return null;
            }
            // Explicitly initialize Conversation and Sender User
            message.getConversation().getId(); // Forces initialization of conversation
            message.getConversation().getUser1().getId(); // Forces initialization of user1
            message.getConversation().getUser2().getId(); // Forces initialization of user2
            message.getSender().getId(); // Forces initialization of sender

            // Explicitly initialize reactions collection
            message.getReactions().size(); // Forces initialization

            return new MessageResponseDTO(
                    message.getId(),
                    message.getConversation().getId(),
                    message.getSender().getId(),
                    message.getSender().getUsername(),
                    message.getSender().getFirstName(),
                    message.getSender().getLastName(),
                    message.getSender().getProfileImageUrl(),
                    message.getContent(),
                    message.getSentAt(),
                    message.isDelivered(),
                    message.isSeen(),
                    message.getSeenAt(),
                    message.getReactions().stream().map(reaction -> new ReactionDTO(reaction.getMessage().getId(), reaction.getReaction(), reaction.getUser().getId())).collect(Collectors.toList())
            );
        });
    }

    @Override
    public void unsendMessage(String messageId, String username) {
        var message = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only unsend your own messages.");
        }

        long minutesSinceSent = java.time.Duration.between(message.getSentAt(), Instant.now()).toMinutes();
        if (minutesSinceSent > 15) {
            throw new IllegalArgumentException("You can only unsend messages within 15 minutes.");
        }

        messageRepo.delete(message);
    }

    @Override
    public void deleteMessageForMe(String messageId, String username) {
        var message = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MessageDeletion deletion = new MessageDeletion();
        deletion.setMessage(message);
        deletion.setUser(user);
        messageDeletionRepository.save(deletion);
    }

    @Override
    public MessageResponseDTO markMessageAsSeen(String messageId, String username) {
        var message = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!message.getConversation().getUser1().getId().equals(user.getId()) &&
            !message.getConversation().getUser2().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User is not part of this conversation.");
        }

        message.setSeen(true);
        message.setSeenAt(Instant.now());
        Message savedMessage = messageRepo.save(message);

        // Explicitly initialize reactions collection
        savedMessage.getReactions().size(); // Forces initialization

        return new MessageResponseDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getSender().getUsername(),
                savedMessage.getSender().getFirstName(),
                savedMessage.getSender().getLastName(),
                savedMessage.getSender().getProfileImageUrl(),
                savedMessage.getContent(),
                savedMessage.getSentAt(),
                savedMessage.isDelivered(),
                savedMessage.isSeen(),
                savedMessage.getSeenAt(),
                savedMessage.getReactions().stream().map(reaction -> new ReactionDTO(reaction.getMessage().getId(), reaction.getReaction(), reaction.getUser().getId())).collect(Collectors.toList())
        );
    }

    @Override
    public List<ConversationListDTO> getUserConversations(String username) {
        var currentUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Conversation> conversations = conversationRepo.findByUser1IdOrUser2Id(currentUser.getId(), currentUser.getId());

        return conversations.stream().map(convo -> {
            User otherUser = convo.getUser1().getId().equals(currentUser.getId()) ? convo.getUser2() : convo.getUser1();

            Message lastMessage = messageRepo.findTopByConversationIdOrderBySentAtDesc(convo.getId())
                    .orElse(null);

            int unseenCount = messageRepo.countByConversationIdAndSeenFalseAndSenderIdNot(convo.getId(), currentUser.getId());

            return new ConversationListDTO(
                    convo.getId(),
                    otherUser.getId(),
                    otherUser.getUsername(),
                    otherUser.getFirstName(),
                    otherUser.getLastName(),
                    otherUser.getProfileImageUrl(),
                    lastMessage != null ? lastMessage.getContent() : null,
                    lastMessage != null ? lastMessage.getSentAt() : null,
                    unseenCount
            );
        }).collect(Collectors.toList());
    }
}