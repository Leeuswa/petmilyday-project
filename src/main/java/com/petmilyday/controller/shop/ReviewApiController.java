package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.product.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final MemberRepository memberRepository;

    // 특정 상품 리뷰 전체 조회 API
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReview>> getProductReviews(@PathVariable Long productId) {
        List<ProductReview> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    // 실제 로그인 회원 인증 및 리뷰 등록 처리 API
    @PostMapping("/register")
    public ResponseEntity<?> registerReview(@RequestBody ReviewWriteDto dto, Principal principal) {
        try {

            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
            }


            String username = principal.getName();


            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            Long memberId = member.getId();


            ProductReview savedReview = reviewService.registerReview(username, memberId, dto);
            return ResponseEntity.ok(savedReview);

        } catch (IllegalArgumentException | IllegalStateException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}