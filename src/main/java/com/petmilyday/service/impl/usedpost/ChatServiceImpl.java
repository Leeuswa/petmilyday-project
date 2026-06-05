package com.petmilyday.service.impl.usedpost;

import com.petmilyday.dto.usedpost.ChatRoomListDTO;
import com.petmilyday.entity.chat.ChatMessage;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.chat.ChatMessageRepository;
import com.petmilyday.entity.chat.ChatRoom;
import com.petmilyday.repository.chat.ChatRoomRepository;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.dto.usedpost.ChatMessageDTO;
import com.petmilyday.service.usedpost.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsedPostRepository usedPostRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public ChatRoom createRoom(Long postId, Long buyerId) {

        return chatRoomRepository.findByPostIdAndBuyerId(postId, buyerId)
                .orElseGet(() -> {

                    UsedPost post = usedPostRepository.findById(postId)
                            .orElseThrow(() -> new RuntimeException("게시글 없음"));

                    if (post.getMember() == null
                            || post.getMember().getId() == null) {
                        throw new RuntimeException("게시글 작성자 정보 없음");
                    }

                    if (post.getMember().getId().equals(buyerId)) {
                        throw new RuntimeException("본인 게시글에는 채팅할 수 없습니다.");
                    }

                    ChatRoom room = new ChatRoom();
                    room.setPostId(postId);
                    room.setBuyerId(buyerId);
                    room.setSellerId(post.getMember().getId());
                    room.setStatus("OPEN");
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
                    dto.setCreatedAt(m.getCreatedAt());
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
        msg.setType("TEXT");
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
        msg.setType("TEXT");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(false);

        chatMessageRepository.save(msg);
    }

    @Override
    public ChatRoom getRoom(Long roomId) {

        return chatRoomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException("채팅방 없음"));
    }

    @Override
    public List<ChatRoomListDTO> getRoomList(Long memberId) {

        List<ChatRoom> rooms =
                chatRoomRepository.findBySellerIdOrBuyerId(
                        memberId,
                        memberId
                );

        List<ChatRoomListDTO> result =
                new ArrayList<>();

        for (ChatRoom room : rooms) {

            if (room.getPostId() == null
                    || room.getBuyerId() == null
                    || room.getSellerId() == null) {
                continue;
            }

            ChatRoomListDTO dto =
                    new ChatRoomListDTO();

            dto.setRoomId(room.getId());
            dto.setPostId(room.getPostId());

            UsedPost post =
                    usedPostRepository.findById(
                            room.getPostId()
                    ).orElse(null);

            if (post != null) {
                dto.setPostTitle(
                        post.getTitle()
                );
            }

            Long opponentId =
                    room.getBuyerId().equals(memberId)
                            ? room.getSellerId()
                            : room.getBuyerId();

            Member opponent =
                    memberRepository.findById(opponentId)
                            .orElse(null);

            if (opponent != null) {

                dto.setOpponentName(
                        opponent.getNickname()
                );
            }

            ChatMessage lastMessage =
                    chatMessageRepository
                            .findTopByRoomIdOrderByCreatedAtDesc(
                                    room.getId()
                            )
                            .orElse(null);

            if (lastMessage != null) {

                dto.setLastMessage(
                        lastMessage.getMessage()
                );

                dto.setLastMessageTime(
                        lastMessage.getCreatedAt()
                );

                long unreadCount =
                        chatMessageRepository
                                .countByRoomIdAndSenderIdNotAndIsReadFalse(
                                        room.getId(),
                                        memberId
                                );

                dto.setUnreadCount(
                        unreadCount
                );
            } else {
                dto.setUnreadCount(0L);
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public void markAsRead(
            Long roomId,
            Long memberId
    ) {

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByRoomIdAndSenderIdNotAndIsReadFalse(
                                roomId,
                                memberId
                        );

        for (ChatMessage msg : messages) {

            msg.setIsRead(true);
        }
    }
}