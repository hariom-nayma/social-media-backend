package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ChatMessageDto;
import com.socialmedia.app.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageDto>> getAllMessages() {
        return ResponseEntity.ok(chatService.getAllMessages());
    }

    // Fetch chat history between two users
    @GetMapping("/{userId}")
    public ResponseEntity<List<ChatMessageDto>> getConversation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String userId) {
        return ResponseEntity.ok(chatService.getConversation(user.getUsername(), userId));
    }

    // Send image message
    @PostMapping("/send-image/{toUserId}")
    public ResponseEntity<ChatMessageDto> sendImage(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String toUserId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(chatService.sendImageMessage(user.getUsername(), toUserId, file));
    }
//    @MessageMapping("/chat/delivered")
//    public void markDelivered(ChatDeliveryEvent event) {
//        redisTemplate.opsForSet().add("chat:delivered:" + event.getChatId() + ":" + event.getMessageId(), event.getUserId());
//        messagingTemplate.convertAndSend("/topic/chat/" + event.getChatId() + "/delivered", event);
//    }
//
//    @MessageMapping("/chat/seen")
//    public void markSeen(ChatSeenEvent event) {
//        redisTemplate.opsForSet().add("chat:seen:" + event.getChatId() + ":" + event.getMessageId(), event.getUserId());
//        messagingTemplate.convertAndSend("/topic/chat/" + event.getChatId() + "/seen", event);
//    }

}
