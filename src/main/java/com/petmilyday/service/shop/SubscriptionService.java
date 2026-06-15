package com.petmilyday.service.shop;

import com.petmilyday.dto.shop.SubscriptionRequestDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import java.util.List;

public interface SubscriptionService {
    List<SubscriptionResponseDto> getActiveSubscriptions(String username);

    Long createSubscription(SubscriptionRequestDto requestDto, String username);

    void changeCycle(Long id, int newCycle);

    void cancelSubscription(Long id);
}