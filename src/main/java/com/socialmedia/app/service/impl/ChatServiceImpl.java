package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.ChatMessageDto;
import com.socialmedia.app.model.ChatMessage;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.ChatMessageRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper mapper;

    public ChatServiceImpl(ChatMessageRepository chatRepository, UserRepository userRepository,
                           CloudinaryService cloudinaryService, ModelMapper mapper) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.mapper = mapper;
    }

    @Override
    @CacheEvict(value = "messages", allEntries = true)
    public ChatMessageDto saveMessage(ChatMessageDto dto, String senderId) {
         String senderUuid = senderId;
        User sender = userRepository.findById(senderUuid)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        User receiver = userRepository.findById(dto.getToUserId())
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText(dto.getText());
        msg.setMediaUrl(dto.getMediaUrl());
        msg.setCreatedAt(Instant.now());

        chatRepository.save(msg);

        ChatMessageDto saved = mapper.map(msg, ChatMessageDto.class);
        saved.setFromUserId(sender.getId());
        saved.setToUserId(receiver.getId());
        saved.setCreatedAt(msg.getCreatedAt());
        return saved;
    }

    @Override
    public List<ChatMessageDto> getConversation(String user1Id, String user2Id) {
        User u1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User u2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return chatRepository.findConversation(u1, u2).stream()
                .map(m -> {
                    ChatMessageDto dto = mapper.map(m, ChatMessageDto.class);
                    dto.setFromUserId(m.getSender().getId());
                    dto.setToUserId(m.getReceiver().getId());
                    dto.setCreatedAt(m.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "messages", allEntries = true)
    public ChatMessageDto sendImageMessage(String senderId, String receiverId, MultipartFile image) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        String imageUrl = cloudinaryService.uploadImage(image);

        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setMediaUrl(imageUrl);
        msg.setCreatedAt(Instant.now());

        chatRepository.save(msg);

        ChatMessageDto dto = mapper.map(msg, ChatMessageDto.class);
        dto.setFromUserId(senderId);
        dto.setToUserId(receiverId);
        dto.setMediaUrl(imageUrl);
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }
    
    @Override
    @Cacheable(value = "messages")
    public List<ChatMessageDto> getAllMessages() {
        return chatRepository.findAll().stream()
                .map(m -> {
                    ChatMessageDto dto = mapper.map(m, ChatMessageDto.class);
                    dto.setFromUserId(m.getSender().getId());
                    dto.setToUserId(m.getReceiver().getId());
                    dto.setCreatedAt(m.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
