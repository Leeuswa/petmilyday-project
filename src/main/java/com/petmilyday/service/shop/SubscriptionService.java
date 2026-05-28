package com.petmilyday.service.shop;

import com.petmilyday.dto.shop.SubscriptionRequestDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import java.util.List;

public interface SubscriptionService {
    // 현재 로그인한 회원의 활성화된(ACTIVE) 정기구독 목록만 가져오기 (username으로 통일)
    List<SubscriptionResponseDto> getActiveSubscriptions(String username);

    // 정기구독 신청
    Long createSubscription(SubscriptionRequestDto requestDto, String username);

    // [★추가] 배송 주기 변경
    void changeCycle(Long id, int newCycle);

    // [★추가] 정기구독 해지
    void cancelSubscription(Long id);
}