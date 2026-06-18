package com.petmilyday.dto.cart;

import com.petmilyday.entity.cart.CartItem;
import lombok.Getter;

@Getter
public class CartItemResponseDto {
    private Long cartItemId;
    private Long productId;
    private String name;
    private Integer price;
    private String imgUrl;
    private Integer quantity;

    // 장바구니 엔티티 데이터를 화면 반환용 DTO로 변환
    public CartItemResponseDto(CartItem entity) {
        this.cartItemId = entity.getId();
        this.productId = entity.getProduct().getId();
        this.name = entity.getProduct().getName();
        this.price = entity.getProduct().getPrice();
        this.imgUrl = entity.getProduct().getImgUrl();
        this.quantity = entity.getQuantity();
    }
}