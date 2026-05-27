package com.petmilyday.domain.chat;

import com.petmilyday.domain.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomId(Long roomId);
}
