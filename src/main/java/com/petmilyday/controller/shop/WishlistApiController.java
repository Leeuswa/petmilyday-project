package com.petmilyday.controller.shop;

import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductWishlistRepository;
import com.petmilyday.service.product.ProductWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final ProductWishlistService wishlistService;
    private final ProductWishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;

    // 위시리스트 토글 (등록 / 해제)
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody Map<String, Long> payload, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long productId = payload.get("productId");
        boolean isLiked = wishlistService.toggleWishlist(productId, principal.getName());

        return ResponseEntity.ok(Map.of("success", true, "isLiked", isLiked));
    }

    // 로그인한 회원이 찜한 상품 ID 리스트 조회
    @GetMapping("/my")
    public ResponseEntity<List<Long>> getMyWishlist(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(List.of());
        }

        com.petmilyday.entity.member.Member member = memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        List<Long> likedIds = wishlistRepository.findAll()
                .stream()
                .filter(w -> w.getMember().getId().equals(member.getId()))
                .map(w -> w.getProduct().getId())
                .collect(Collectors.toList());

        return ResponseEntity.ok(likedIds);
    }
}