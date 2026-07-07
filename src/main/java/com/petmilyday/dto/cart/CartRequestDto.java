package com.petmilyday.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartRequestDto {
    private Long productId;
    private Integer quantity;
}