package com.petmilyday.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewWriteDto {
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String imgUrl;
}