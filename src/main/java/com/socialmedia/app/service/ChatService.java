package com.socialmedia.app.service;

import com.socialmedia.app.dto.ChatMessageDto;
import com.socialmedia.app.dto.ReactionDTO;
import com.socialmedia.app.model.Message;
import com.socialmedia.app.dto.ConversationListDTO;
import com.socialmedia.app.dto.MessageResponseDTO;
import com.socialmedia.app.model.MessageReaction;
import org.springframework.data.domain.Page;
import java.util.List;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ChatService {
    MessageResponseDTO sendMessage(ChatMessageDto dto);
    MessageReaction reactToMessage(ReactionDTO dto);
    Page<MessageResponseDTO> getMessages(String conversationId, int page, int size, String username);
    void unsendMessage(String messageId, String username);
    void deleteMessageForMe(String messageId, String username);
    MessageResponseDTO markMessageAsSeen(String messageId, String username);
    List<ConversationListDTO> getUserConversations(String username);
    // ConversationListDTO getConversationBetweenUsers(String name, String id);
}
