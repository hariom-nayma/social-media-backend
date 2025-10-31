package com.socialmedia.app.controller;

import com.socialmedia.app.dto.ConversationListDTO;
import com.socialmedia.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationListDTO>> getUserConversations(Principal principal) {
        return ResponseEntity.ok(chatService.getUserConversations(principal.getName()));
    }
    
}
