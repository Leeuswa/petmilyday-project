package com.petmilyday.dto.usedpost;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomListDTO {

    private Long roomId;

    private Long postId;

    private String postTitle;

    private String opponentName;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Long unreadCount;
}