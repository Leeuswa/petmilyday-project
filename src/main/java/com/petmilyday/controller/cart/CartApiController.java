package com.petmilyday.controller.cart;

import com.petmilyday.dto.cart.CartRequestDto;
import com.petmilyday.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public ResponseEntity<String> addCart(@RequestBody CartRequestDto requestDto) {
        Long mockUserId = 1L;

        try {
            cartService.addCart(mockUserId, requestDto);
            return ResponseEntity.ok("장바구니에 성공적으로 담겼습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 장바구니에 담긴 상품의 수량 수정
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long cartItemId, @RequestBody java.util.Map<String, Integer> request) {
        cartService.updateQuantity(cartItemId, request.get("quantity"));
        return ResponseEntity.ok("수정완료");
    }

    // 장바구니에 선택된 상품 항목들 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteItems(@RequestBody java.util.List<Long> cartItemIds) {
        cartService.deleteCartItems(cartItemIds);
        return ResponseEntity.ok("삭제완료");
    }

    // 장바구니에 담긴 총 상품 개수 조회
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount() {
        Long mockUserId = 1L;
        int count = cartService.getCartItemCount(mockUserId);
        return ResponseEntity.ok(count);
    }
}