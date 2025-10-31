package com.socialmedia.app.service.impl;

import com.socialmedia.app.dto.NotificationDto;
import com.socialmedia.app.model.Notification;
import com.socialmedia.app.enums.NotificationType;
import com.socialmedia.app.model.User;
import com.socialmedia.app.repository.NotificationRepository;
import com.socialmedia.app.repository.UserRepository;
import com.socialmedia.app.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messaging;
    private final ModelMapper mapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   SimpMessagingTemplate messaging,
                                   ModelMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messaging = messaging;
        this.mapper = mapper;
    }

    @Override
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto pushNotification(String toUsername, NotificationType type, String message, String payload) {
        User user = userRepository.findByUsername(toUsername).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        n.setPayload(payload);
        n = notificationRepository.save(n);

        NotificationDto dto = toDto(n);
        // push to connected user destination: note principal name is username string per handshake
        messaging.convertAndSendToUser(toUsername, "/queue/notifications", dto);
        System.out.println("Pushed notification to " + toUsername);
        return dto;
    }

    @Override
    public List<NotificationDto> fetchRecent(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.findTop50ByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "notifications", allEntries = true)
    public void markAsRead(String notificationId) {
        var nOpt = notificationRepository.findById(notificationId);
        nOpt.ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = mapper.map(n, NotificationDto.class);
        return dto;
    }
    
    @Override
    @Cacheable(value = "notifications")
    public List<NotificationDto> getAllNotifications() {
        return notificationRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
}
