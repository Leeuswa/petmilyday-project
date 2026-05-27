package com.petmilyday.dto.usedpost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {

    private Long roomId;
    private Long senderId;
    private String message;
}