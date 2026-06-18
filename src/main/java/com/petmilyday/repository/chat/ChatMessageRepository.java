package com.petmilyday.repository.chat;

import com.petmilyday.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomId(Long roomId);

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

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

    // 안 읽은 메시지 한 번에 읽음 처리
    @Modifying
    @Query("""
        UPDATE ChatMessage m
        SET m.isRead = true
        WHERE m.roomId = :roomId
          AND m.senderId <> :memberId
          AND m.isRead = false
    """)
    int markAsRead(
            @Param("roomId") Long roomId,
            @Param("memberId") Long memberId
    );

    // 여러 채팅방의 마지막 메시지 한 번에 조회
    @Query("""
        SELECT m
        FROM ChatMessage m
        WHERE m.id IN (
            SELECT MAX(m2.id)
            FROM ChatMessage m2
            WHERE m2.roomId IN :roomIds
            GROUP BY m2.roomId
        )
    """)
    List<ChatMessage> findLastMessagesByRoomIds(
            @Param("roomIds") List<Long> roomIds
    );

    // 여러 채팅방의 안 읽은 메시지 개수 한 번에 조회
    @Query("""
        SELECT m.roomId, COUNT(m)
        FROM ChatMessage m
        WHERE m.roomId IN :roomIds
          AND m.senderId <> :memberId
          AND m.isRead = false
        GROUP BY m.roomId
    """)
    List<Object[]> countUnreadByRoomIds(
            @Param("roomIds") List<Long> roomIds,
            @Param("memberId") Long memberId
    );
}