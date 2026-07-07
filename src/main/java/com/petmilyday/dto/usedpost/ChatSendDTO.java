package com.petmilyday.dto.usedpost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendDTO {
    private Long roomId;
    private Long senderId;
    private String message;
}
