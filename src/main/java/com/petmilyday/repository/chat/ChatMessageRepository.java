package com.petmilyday.repository.chat;

import com.petmilyday.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomId(Long roomId);

    Optional<ChatMessage>
    findTopByRoomIdOrderByCreatedAtDesc(
            Long roomId
    );

    long countByRoomIdAndSenderIdNotAndIsReadFalse(
            Long roomId,
            Long senderId
    );

    List<ChatMessage>
    findByRoomIdAndSenderIdNotAndIsReadFalse(
            Long roomId,
            Long senderId
    );
}
