package com.petmilyday.service.notification;

import com.petmilyday.dto.notification.NotificationDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationServiceImpl implements NotificationService {

    // SSE 연결 유지 시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    // 사용자별 SSE 연결 저장소
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String username) {

        // SSE 연결 객체 생성
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 사용자별 연결 목록에 emitter 저장
        emitters.computeIfAbsent(username, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        // 연결 종료, 시간 초과, 에러 발생 시 emitter 제거
        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError((e) -> removeEmitter(username, emitter));

        try {
            // 연결 성공 메시지 전송
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 완료"));
        } catch (IOException e) {
            removeEmitter(username, emitter);
        }

        return emitter;
    }

    @Override
    public void sendToUser(String username, NotificationDTO notificationDTO) {

        // 해당 사용자의 SSE 연결 목록 조회
        List<SseEmitter> userEmitters = emitters.get(username);

        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        // 사용자에게 알림 이벤트 전송
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notificationDTO));
            } catch (IOException e) {
                // 전송 실패한 연결은 제거 대상에 추가
                deadEmitters.add(emitter);
            }
        }

        // 끊어진 연결 제거
        userEmitters.removeAll(deadEmitters);
    }

    @Override
    public void sendToUsers(List<String> usernames, NotificationDTO notificationDTO) {

        // 여러 사용자에게 반복 전송
        for (String username : usernames) {
            sendToUser(username, notificationDTO);
        }
    }

    private void removeEmitter(String username, SseEmitter emitter) {

        // 사용자 연결 목록에서 해당 emitter 제거
        List<SseEmitter> userEmitters = emitters.get(username);

        if (userEmitters != null) {
            userEmitters.remove(emitter);

            // 남은 연결이 없으면 사용자 데이터 제거
            if (userEmitters.isEmpty()) {
                emitters.remove(username);
            }
        }
    }
}