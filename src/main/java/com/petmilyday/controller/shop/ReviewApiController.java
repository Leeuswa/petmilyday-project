package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.product.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final MemberRepository memberRepository;

    // 특정 상품의 전체 리뷰 목록 조회
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReview>> getProductReviews(@PathVariable Long productId) {
        List<ProductReview> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    // 로그인 회원 인증 및 새 리뷰 등록
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

    // 작성자 본인 검증 또는 관리자 권한 확인 후 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
            }

            String username = principal.getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            Long currentMemberId = member.getId();

            boolean isAdmin = false;
            if (principal instanceof Authentication) {
                Authentication auth = (Authentication) principal;
                isAdmin = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ROLE_ADMIN"));
            }

            reviewService.deleteReview(reviewId, currentMemberId, isAdmin);

            return ResponseEntity.ok().body(Map.of("success", true, "message", "리뷰가 성공적으로 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 삭제 중 알 수 없는 오류 발생");
        }
    }
}