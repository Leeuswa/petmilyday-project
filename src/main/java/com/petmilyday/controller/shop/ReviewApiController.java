package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.service.product.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReview>> getProductReviews(@PathVariable Long productId) {
        List<ProductReview> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 등록 처리 API
    @PostMapping("/register")
    public ResponseEntity<?> registerReview(@RequestBody ReviewWriteDto dto) {
        try {
            Long mockMemberId = 1L;
            ProductReview savedReview = reviewService.registerReview(mockMemberId, dto);
            return ResponseEntity.ok(savedReview);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}