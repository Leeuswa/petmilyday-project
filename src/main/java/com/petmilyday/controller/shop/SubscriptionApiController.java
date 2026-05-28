package com.petmilyday.controller.shop;

import com.petmilyday.dto.shop.SubscriptionRequestDto;
import com.petmilyday.service.shop.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription") // 클래스 기본 주소: /api/subscription
public class SubscriptionApiController {

    private final SubscriptionService subscriptionService;

    // 1. 구독 신청 (최종주소: /api/subscription/subscribe)
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionRequestDto requestDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요한 서비스입니다.");
        }

        try {
            Long subscriptionId = subscriptionService.createSubscription(requestDto, principal.getName());
            return ResponseEntity.ok().body("정기구독 신청 성공! 주문번호: " + subscriptionId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 주기 변경 (최종주소: /api/subscription/{id}/change-cycle)
    // [★완벽수정] 중복 경로 다 걷어내고 딱 깔끔하게 변수값과 매핑 주소만 매칭!
    @PutMapping("/{id}/change-cycle")
    public ResponseEntity<?> changeCycle(@PathVariable("id") Long id, @RequestParam("cycleDays") int cycleDays) {
        try {
            subscriptionService.changeCycle(id, cycleDays);
            return ResponseEntity.ok().body("배송 주기가 " + cycleDays + "일로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 구독 해지 (최종주소: /api/subscription/{id}/cancel)
    // [★완벽수정] 중복 경로 싹 다 잡아서 정상화 완료!
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable("id") Long id) {
        try {
            subscriptionService.cancelSubscription(id);
            return ResponseEntity.ok().body("정기구독이 정상적으로 해지되었습니다. 😢");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}