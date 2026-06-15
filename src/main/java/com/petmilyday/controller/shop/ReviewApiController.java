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

    // 🎯 [신규 추가] 본인확인 또는 관리자 권한 프리패스 리뷰 삭제 API
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
            }

            // 1. 현재 로그인한 사용자 정보 조회 (memberId 추출용)
            String username = principal.getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            Long currentMemberId = member.getId();

            // 2. 스프링 시큐리티 권한 정보에서 관리자(ROLE_ADMIN) 권한이 있는지 추출
            boolean isAdmin = false;
            if (principal instanceof Authentication) {
                Authentication auth = (Authentication) principal;
                isAdmin = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ROLE_ADMIN")); // 네 프로젝트 권한 스트링 포맷에 맞게 조절 가능
            }

            // 3. 서비스 단으로 슛해서 본인인증 OR 관리자 검증 후 데이터 삭제
            reviewService.deleteReview(reviewId, currentMemberId, isAdmin);

            // 4. 프론트앤드 타임리프 fetch 쪽에 성공 메시지 리턴
            return ResponseEntity.ok().body(Map.of("success", true, "message", "리뷰가 성공적으로 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 삭제 중 알 수 없는 오류 발생");
        }
    }
}