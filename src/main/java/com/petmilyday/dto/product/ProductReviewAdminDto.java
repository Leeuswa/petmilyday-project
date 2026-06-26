package com.petmilyday.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductReviewAdminDto {

    private Long id;
    private String productName;
    private String memberDisplayName;
    private Integer rating;
    private String content;
    private String imgUrl;
    private LocalDateTime createdAt;
    private Boolean isReported;
}
