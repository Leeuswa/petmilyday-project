package com.petmilyday.service.usedpost;

import com.petmilyday.domain.chat.ChatMessage;
import com.petmilyday.domain.chat.ChatRoom;
import com.petmilyday.dto.usedpost.ChatMessageDTO;

import java.util.List;

public interface ChatService {

    ChatRoom createRoom(Long postId, Long buyerId);

    List<ChatRoom> getRooms(Long userId);

    List<ChatMessageDTO> getMessages(Long roomId);

    void sendMessage(Long roomId, Long senderId, String message);

    void saveMessage(ChatMessageDTO dto);
}
