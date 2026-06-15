package com.petmilyday.controller.usedpost;

import com.petmilyday.entity.chat.ChatRoom;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.MannerScoreRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MemberRepository memberRepository;
    private final UsedPostRepository usedPostRepository;
    private final MannerScoreRepository mannerScoreRepository;

    @PostMapping("/chat/room")
    public String createRoom(
            @RequestParam Long postId,
            Authentication authentication
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        ChatRoom room =
                chatService.createRoom(
                        postId,
                        member.getId()
                );

        return "redirect:/chat/room/" + room.getId();
    }

    @GetMapping("/chat/list")
    public String roomList(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        model.addAttribute(
                "rooms",
                chatService.getRoomList(member.getId())
        );

        return "used/chat-list";
    }

    @GetMapping("/chat/room/{roomId}")
    public String room(
            @PathVariable Long roomId,
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        ChatRoom room =
                chatService.getRoom(roomId);

        if (!room.getBuyerId().equals(member.getId())
                && !room.getSellerId().equals(member.getId())) {

            return "redirect:/chat/list";
        }

        chatService.markAsRead(
                roomId,
                member.getId()
        );

        UsedPost post =
                usedPostRepository.findById(room.getPostId())
                        .orElse(null);

        Long targetMemberId =
                room.getBuyerId().equals(member.getId())
                        ? room.getSellerId()
                        : room.getBuyerId();


        Member opponent =
                memberRepository.findById(targetMemberId)
                        .orElse(null);

        boolean alreadyEvaluated =
                mannerScoreRepository.existsByFromMember_IdAndToMember_IdAndUsedPost_Id(
                        member.getId(),
                        targetMemberId,
                        room.getPostId()
                );

        boolean isRealBuyer =
                post != null
                        && post.getBuyerId() != null
                        && post.getBuyerId().equals(member.getId());

        boolean isSeller =
                post != null
                        && post.getMember() != null
                        && post.getMember().getId().equals(member.getId());

        boolean canEvaluate =
                post != null
                        && post.getStatus() == UsedPostStatus.SOLD
                        && (isRealBuyer || isSeller)
                        && !alreadyEvaluated;

        model.addAttribute("roomId", roomId);
        model.addAttribute("senderId", member.getId());
        model.addAttribute("postId", room.getPostId());
        model.addAttribute("targetMemberId", targetMemberId);
        model.addAttribute("canEvaluate", canEvaluate);

        model.addAttribute(
                "messages",
                chatService.getMessages(roomId)
        );

        model.addAttribute(
                "postTitle",
                post != null ? post.getTitle() : "삭제된 게시글"
        );

        model.addAttribute(
                "opponentName",
                opponent != null
                        ? opponent.getNickname()
                        : "알 수 없음"
        );

        return "used/chat";
    }

    @PostMapping("/chat/message")
    public String sendMessage(
            @RequestParam Long roomId,
            @RequestParam Long senderId,
            @RequestParam String message
    ) {

        chatService.sendMessage(
                roomId,
                senderId,
                message
        );

        return "redirect:/chat/room/" + roomId;
    }
}