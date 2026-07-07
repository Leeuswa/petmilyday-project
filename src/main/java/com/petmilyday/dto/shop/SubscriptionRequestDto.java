package com.petmilyday.dto.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class SubscriptionRequestDto {
    private Long productId; // 구독할 상품 ID
    private int quantity;   // 수량
    private int cycleDays;  // 배송 주기 (14일 / 30일 등)
}