package com.petmilyday.dto.usedpost;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDTO {

    private Long roomId;

    private Long senderId;

    private String senderName;

    private String message;

    private LocalDateTime createdAt;
}