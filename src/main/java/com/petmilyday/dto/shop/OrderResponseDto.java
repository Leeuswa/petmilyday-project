package com.petmilyday.dto.shop;

import com.petmilyday.entity.shop.Orders; // Orders로 임포트 확인!
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String productName;
    private int totalPrice;
    private String orderStatus;
    private LocalDateTime orderDate;
    private int quantity;
    private boolean isDeleted;

    public OrderResponseDto(Orders order) {
        this.orderId = order.getId();

        // 💡 1. 상품명은 orderItems 리스트의 첫 번째 것에서 가져오기
        if (!order.getOrderItems().isEmpty()) {
            this.productName = order.getOrderItems().get(0).getProduct().getName();
            this.isDeleted = order.getOrderItems().get(0).getProduct().isDeleted();
            this.quantity = order.getOrderItems().get(0).getQuantity();
        } else {
            this.productName = "상품 정보 없음";
        }

        this.totalPrice = order.getTotalPrice();
        this.orderStatus = order.getStatus();      // orderStatus -> status로 변경
        this.orderDate = order.getCreatedAt();    // orderDate -> createdAt으로 변경
    }
}