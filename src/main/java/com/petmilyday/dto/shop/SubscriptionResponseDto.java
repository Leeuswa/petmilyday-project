package com.petmilyday.dto.shop;

import com.petmilyday.entity.shop.Subscription;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class SubscriptionResponseDto {
    private Long id;
    private String productName;      // 상품명 (예: 강아지 오리고기 사료 2kg)
    private Integer productPrice;    // 가격
    private String imgUrl;           // 상품 이미지
    private int quantity;            // 수량
    private int cycleDays;           // 배송 주기 (14일, 30일)
    private LocalDate nextDeliveryDate; // 다음 배송일 (05.25 배송)
    private String status;           // 상태 (ACTIVE)

    // 엔티티를 DTO로 슥 변환해주는 생성자
    public SubscriptionResponseDto(Subscription subscription) {
        this.id = subscription.getId();
        this.productName = subscription.getProduct().getName();
        this.productPrice = subscription.getProduct().getPrice();
        this.imgUrl = subscription.getProduct().getImgUrl();
        this.quantity = subscription.getQuantity();
        this.cycleDays = subscription.getCycleDays();
        this.nextDeliveryDate = subscription.getNextDeliveryDate();
        this.status = subscription.getStatus().name();
    }
}