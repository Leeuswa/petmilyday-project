package com.petmilyday.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewWriteDto { // 🟢 직관적으로 '리뷰 저장 DTO'로 네이밍 세탁 완료!
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String imgUrl;
}