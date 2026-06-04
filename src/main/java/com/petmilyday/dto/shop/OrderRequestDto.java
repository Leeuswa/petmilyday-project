package com.petmilyday.dto.shop;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class OrderRequestDto {
    private String deliveryAddress;
    private String paymentMethod;
    private List<OrderItemDto> items;
}