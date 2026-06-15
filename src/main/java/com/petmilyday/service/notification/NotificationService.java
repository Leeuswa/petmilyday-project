package com.petmilyday.service.notification;

import com.petmilyday.dto.notification.NotificationDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {

    // 로그인한 사용자의 SSE 연결을 등록
    SseEmitter subscribe(String username);

    // 특정 사용자 한 명에게 알림 전송
    void sendToUser(String username, NotificationDTO notificationDTO);

    // 여러 사용자에게 알림 전송
    void sendToUsers(List<String> usernames, NotificationDTO notificationDTO);
}