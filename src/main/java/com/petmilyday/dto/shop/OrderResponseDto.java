package com.petmilyday.dto.shop;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderResponseDto {
    private Long orderId;            // 주문 고유 번호
    private String productName;      // 대표 상품명 (예: 강아지 사료 외 1건)
    private int totalPrice;          // 총 결제 금액
    private String orderStatus;      // 주문 상태 (결제완료, 배송중 등)
    private LocalDateTime orderDate; // 주문 일시
    private int quantity;
}