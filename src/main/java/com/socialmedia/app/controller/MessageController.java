package com.socialmedia.app.controller;

import com.socialmedia.app.dto.MessageResponseDTO;
import com.socialmedia.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    @GetMapping("/{conversationId}")
    public ResponseEntity<Page<MessageResponseDTO>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            java.security.Principal principal) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size, principal.getName()));
    }
}
