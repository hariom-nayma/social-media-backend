package com.socialmedia.app.service;

import com.socialmedia.app.dto.ChatMessageDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ChatService {
    ChatMessageDto saveMessage(ChatMessageDto dto, String senderId);
    List<ChatMessageDto> getConversation(String user1Id, String user2Id);
    ChatMessageDto sendImageMessage(String senderId, String receiverId, MultipartFile image);
    List<ChatMessageDto> getAllMessages();
}
