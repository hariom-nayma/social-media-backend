package com.socialmedia.app.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDeliveryEvent {
    private UUID chatId;
    private UUID messageId;
    private UUID userId;
    private String type = "DELIVERED";
}

