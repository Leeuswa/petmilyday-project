package com.petmilyday.dto.shop;

import com.petmilyday.entity.shop.Subscription;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class SubscriptionResponseDto {
    private Long id;
    private String productName;
    private Integer productPrice;
    private String imgUrl;
    private int quantity;
    private int cycleDays;
    private LocalDate nextDeliveryDate;
    private String status;
    private boolean isDeleted;

    public SubscriptionResponseDto(Subscription subscription) {
        this.id = subscription.getId();
        this.productName = subscription.getProduct().getName();
        this.productPrice = subscription.getProduct().getPrice();
        this.imgUrl = subscription.getProduct().getImgUrl();
        this.quantity = subscription.getQuantity();
        this.cycleDays = subscription.getCycleDays();
        this.nextDeliveryDate = subscription.getNextDeliveryDate();
        this.status = subscription.getStatus().name();

        this.isDeleted = subscription.getProduct().isDeleted();
    }
}