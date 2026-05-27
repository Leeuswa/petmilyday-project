package com.petmilyday.controller.usedpost;

import com.petmilyday.domain.chat.ChatRoom;
import com.petmilyday.service.usedpost.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. 채팅방 생성 → 이동
    @PostMapping("/chat/room")
    public String createRoom(@RequestParam Long postId,
                             @RequestParam Long buyerId) {

        ChatRoom room = chatService.createRoom(postId, buyerId);

        return "redirect:/chat/room/" + room.getId();
    }

    // 2. 채팅방 목록
    @GetMapping("/chat/list")
    public String roomList(@RequestParam Long userId, Model model) {

        model.addAttribute("rooms", chatService.getRooms(userId));

        return "used/chat"; // ⭐ 수정
    }

    @GetMapping("/chat/room/{roomId}")
    public String room(@PathVariable Long roomId, Model model) {

        model.addAttribute("roomId", roomId);
        model.addAttribute("messages", chatService.getMessages(roomId));

        return "used/chat"; // ⭐ 수정
    }

    // 4. 메시지 전송 (HTTP 테스트용)
    @PostMapping("/chat/message")
    public String sendMessage(@RequestParam Long roomId,
                              @RequestParam Long senderId,
                              @RequestParam String message) {

        chatService.sendMessage(roomId, senderId, message);

        return "redirect:/chat/room/" + roomId;
    }
}