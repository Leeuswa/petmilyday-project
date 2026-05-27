package com.petmilyday.controller.usedpost;


import com.petmilyday.dto.usedpost.ChatMessageDTO;
import com.petmilyday.service.usedpost.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(ChatMessageDTO dto) {

        // 1. DB 저장
        chatService.saveMessage(dto);

        // 2. 실시간 전송
        template.convertAndSend(
                "/topic/chat/room/" + dto.getRoomId(),
                dto
        );
    }
}
