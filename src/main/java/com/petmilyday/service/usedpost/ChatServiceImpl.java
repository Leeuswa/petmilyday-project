package com.petmilyday.service.usedpost;

import com.petmilyday.domain.chat.ChatMessage;
import com.petmilyday.domain.chat.ChatMessageRepository;
import com.petmilyday.domain.chat.ChatRoom;
import com.petmilyday.domain.chat.ChatRoomRepository;
import com.petmilyday.domain.used.UsedPost;
import com.petmilyday.domain.used.UsedPostRepository;
import com.petmilyday.dto.usedpost.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsedPostRepository usedPostRepository;

    @Override
    @Transactional
    public ChatRoom createRoom(Long postId, Long buyerId) {

        return chatRoomRepository.findByPostIdAndBuyerId(postId, buyerId)
                .orElseGet(() -> {

                    UsedPost post = usedPostRepository.findById(postId)
                            .orElseThrow(() -> new RuntimeException("게시글 없음"));

                    ChatRoom room = new ChatRoom();
                    room.setPostId(postId);
                    room.setBuyerId(buyerId);
                    room.setSellerId(post.getMember().getId());
                    room.setCreatedAt(LocalDateTime.now());

                    return chatRoomRepository.save(room);
                });
    }

    @Override
    public List<ChatRoom> getRooms(Long userId) {
        return chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
    }

    @Override
    public List<ChatMessageDTO> getMessages(Long roomId) {

        return chatMessageRepository.findByRoomId(roomId)
                .stream()
                .map(m -> {
                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setRoomId(m.getRoomId());
                    dto.setSenderId(m.getSenderId());
                    dto.setMessage(m.getMessage());
                    return dto;
                }).toList();
    }

    @Override
    @Transactional
    public void sendMessage(Long roomId, Long senderId, String message) {

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setMessage(message);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(false);

        chatMessageRepository.save(msg);
    }

    @Override
    public void saveMessage(ChatMessageDTO dto) {

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(dto.getRoomId());
        msg.setSenderId(dto.getSenderId());
        msg.setMessage(dto.getMessage());
        msg.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(msg);
    }
}
