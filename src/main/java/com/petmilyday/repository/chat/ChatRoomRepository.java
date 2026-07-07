package com.petmilyday.repository.chat;

import com.petmilyday.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByPostIdAndBuyerId(Long postId, Long buyerId);

    List<ChatRoom> findBySellerIdOrBuyerId(Long sellerId, Long buyerId);
}