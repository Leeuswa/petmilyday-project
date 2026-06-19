package com.petmilyday.service.usedpost;

import com.petmilyday.dto.usedpost.ChatRoomListDTO;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import com.petmilyday.entity.chat.ChatMessage;
import com.petmilyday.entity.chat.ChatRoom;
import com.petmilyday.dto.usedpost.ChatMessageDTO;
import com.petmilyday.entity.used.ItemCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    ChatRoom createRoom(Long postId, Long buyerId);

    List<ChatRoom> getRooms(Long userId);

    List<ChatMessageDTO> getMessages(Long roomId);

    void sendMessage(Long roomId, Long senderId, String message);

    void saveMessage(ChatMessageDTO dto);

    List<ChatRoomListDTO> getRoomList(Long memberId);

    ChatRoom getRoom(Long roomId);

    void markAsRead(
            Long roomId,
            Long memberId
    );
}
