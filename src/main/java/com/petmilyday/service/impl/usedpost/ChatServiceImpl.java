package com.petmilyday.service.impl.usedpost;

import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.dto.usedpost.ChatMessageDTO;
import com.petmilyday.dto.usedpost.ChatRoomListDTO;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import com.petmilyday.entity.chat.ChatMessage;
import com.petmilyday.entity.chat.ChatRoom;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.repository.chat.ChatMessageRepository;
import com.petmilyday.repository.chat.ChatRoomRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.notification.NotificationService;
import com.petmilyday.service.usedpost.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsedPostRepository usedPostRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ChatRoom createRoom(Long postId, Long buyerId) {

        return chatRoomRepository.findByPostIdAndBuyerId(postId, buyerId)
                .orElseGet(() -> {

                    UsedPost post =
                            usedPostRepository.findById(postId)
                                    .orElseThrow(() ->
                                            new RuntimeException("게시글 없음"));

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

        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(m -> {
                    ChatMessageDTO dto = new ChatMessageDTO();

                    dto.setRoomId(m.getRoomId());
                    dto.setSenderId(m.getSenderId());
                    dto.setMessage(m.getMessage());
                    dto.setCreatedAt(m.getCreatedAt());

                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void sendMessage(
            Long roomId,
            Long senderId,
            String message
    ) {

        ChatMessage msg = new ChatMessage();

        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setMessage(message);
        msg.setType("TEXT");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(false);

        chatMessageRepository.save(msg);

        notifyOpponent(roomId, senderId, message);
    }

    @Override
    @Transactional
    public void saveMessage(ChatMessageDTO dto) {

        ChatMessage msg = new ChatMessage();

        msg.setRoomId(dto.getRoomId());
        msg.setSenderId(dto.getSenderId());
        msg.setMessage(dto.getMessage());
        msg.setType("TEXT");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(false);

        chatMessageRepository.save(msg);

        notifyOpponent(dto.getRoomId(), dto.getSenderId(), dto.getMessage());
    }

    // 채팅 메시지를 받는 상대방에게 실시간 알림 전송
    private void notifyOpponent(Long roomId, Long senderId, String message) {

        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);

        if (room == null || room.getBuyerId() == null || room.getSellerId() == null) {
            return;
        }

        Long recipientId =
                room.getBuyerId().equals(senderId) ? room.getSellerId() : room.getBuyerId();

        Member recipient = memberRepository.findById(recipientId).orElse(null);

        if (recipient == null) {
            return;
        }

        Member sender = memberRepository.findById(senderId).orElse(null);

        String senderName =
                sender != null && sender.getNickname() != null && !sender.getNickname().isBlank()
                        ? sender.getNickname()
                        : "상대방";

        String preview =
                message != null && message.length() > 30
                        ? message.substring(0, 30) + "..."
                        : message;

        try {
            notificationService.sendToUser(
                    recipient.getUsername(),
                    NotificationDTO.builder()
                            .type("NEW_CHAT_MESSAGE")
                            .message(senderName + "님이 메시지를 보냈습니다: " + preview)
                            .url("/chat/room/" + roomId)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("채팅 알림 전송 실패 - roomId: {}, recipient: {}", roomId, recipient.getUsername(), e);
        }
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

        if (rooms == null || rooms.isEmpty()) {
            return List.of();
        }

        // 정상 채팅방만 1차 필터링
        List<ChatRoom> validRooms =
                rooms.stream()
                        .filter(room ->
                                room.getId() != null
                                        && room.getPostId() != null
                                        && room.getBuyerId() != null
                                        && room.getSellerId() != null
                        )
                        .toList();

        if (validRooms.isEmpty()) {
            return List.of();
        }

        // 게시글 id 목록
        List<Long> postIds =
                validRooms.stream()
                        .map(ChatRoom::getPostId)
                        .distinct()
                        .toList();

        // 게시글 한 번에 조회
        Map<Long, UsedPost> postMap =
                usedPostRepository.findAllById(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UsedPost::getId,
                                post -> post
                        ));

        // 삭제/숨김 게시글 채팅방은 여기서 먼저 제외
        List<ChatRoom> visibleRooms =
                validRooms.stream()
                        .filter(room -> {
                            UsedPost post =
                                    postMap.get(room.getPostId());

                            return post != null
                                    && !Boolean.TRUE.equals(post.getIsHidden());
                        })
                        .toList();

        if (visibleRooms.isEmpty()) {
            return List.of();
        }

        // 여기부터는 화면에 보일 채팅방만 기준으로 조회
        List<Long> roomIds =
                visibleRooms.stream()
                        .map(ChatRoom::getId)
                        .toList();

        List<Long> opponentIds =
                visibleRooms.stream()
                        .map(room ->
                                room.getBuyerId().equals(memberId)
                                        ? room.getSellerId()
                                        : room.getBuyerId()
                        )
                        .distinct()
                        .toList();

        // 상대방 회원 한 번에 조회
        Map<Long, Member> memberMap =
                memberRepository.findAllById(opponentIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Member::getId,
                                member -> member
                        ));

        // 마지막 메시지 한 번에 조회
        Map<Long, ChatMessage> lastMessageMap =
                chatMessageRepository.findLastMessagesByRoomIds(roomIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ChatMessage::getRoomId,
                                message -> message
                        ));

        // 안 읽은 메시지 개수 한 번에 조회
        Map<Long, Long> unreadCountMap =
                new HashMap<>();

        List<Object[]> unreadRows =
                chatMessageRepository.countUnreadByRoomIds(
                        roomIds,
                        memberId
                );

        for (Object[] row : unreadRows) {

            Long roomId =
                    ((Number) row[0]).longValue();

            Long count =
                    ((Number) row[1]).longValue();

            unreadCountMap.put(roomId, count);
        }

        List<ChatRoomListDTO> result =
                new ArrayList<>();

        for (ChatRoom room : visibleRooms) {

            ChatRoomListDTO dto =
                    new ChatRoomListDTO();

            dto.setRoomId(room.getId());
            dto.setPostId(room.getPostId());

            UsedPost post =
                    postMap.get(room.getPostId());

            dto.setPostTitle(post.getTitle());

            Long opponentId =
                    room.getBuyerId().equals(memberId)
                            ? room.getSellerId()
                            : room.getBuyerId();

            Member opponent =
                    memberMap.get(opponentId);

            if (opponent != null) {

                String opponentName =
                        opponent.getNickname() != null
                                && !opponent.getNickname().isBlank()
                                ? opponent.getNickname()
                                : opponent.getUsername();

                dto.setOpponentName(opponentName);

            } else {

                dto.setOpponentName("알 수 없음");
            }

            ChatMessage lastMessage =
                    lastMessageMap.get(room.getId());

            if (lastMessage != null) {

                dto.setLastMessage(lastMessage.getMessage());
                dto.setLastMessageTime(lastMessage.getCreatedAt());

            } else {

                dto.setLastMessage("");
                dto.setLastMessageTime(null);
            }

            dto.setUnreadCount(
                    unreadCountMap.getOrDefault(
                            room.getId(),
                            0L
                    )
            );

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

        chatMessageRepository.markAsRead(
                roomId,
                memberId
        );
    }


}