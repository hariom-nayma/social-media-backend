package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ChatMessageDto;
import com.socialmedia.app.dto.MessageResponseDTO;
import com.socialmedia.app.dto.TypingDTO;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import com.socialmedia.app.dto.ReactionDTO;
import com.socialmedia.app.model.Message;
import com.socialmedia.app.model.MessageReaction;
import com.socialmedia.app.service.ChatService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

 @Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage( @Payload ChatMessageDto chatMessage) {
        MessageResponseDTO saved = chatService.sendMessage(chatMessage);

        // Notify recipient
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                saved
        );
    }

    @MessageMapping("/chat.delete")
    public void deleteMessageForMe(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        chatService.deleteMessageForMe(messageId, username);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingDTO dto) {
        messagingTemplate.convertAndSendToUser(dto.getRecipientId(), "/queue/typing", dto);
    }

    @MessageMapping("/chat.seen")
    public void markMessageAsSeen(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        MessageResponseDTO message = chatService.markMessageAsSeen(messageId, username);

        // Notify sender that the message has been seen
        messagingTemplate.convertAndSendToUser(
                message.getSenderUsername(),
                "/queue/messages.seen",
                message
        );
    }
}