package com.petmilyday.service.shop;

import com.petmilyday.dto.shop.SubscriptionRequestDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import java.util.List;

// 정기구독 비즈니스 로직 처리를 위한 서비스 인터페이스
public interface SubscriptionService {

    // 회원의 활성화된 정기구독 목록 조회
    List<SubscriptionResponseDto> getActiveSubscriptions(String username);

    // 새로운 정기구독 내역 생성
    Long createSubscription(SubscriptionRequestDto requestDto, String username);

    // 정기구독 배송 주기 변경
    void changeCycle(Long id, int newCycle);

    // 정기구독 해지 처리
    void cancelSubscription(Long id);
}