package com.petmilyday.controller.cart;

import com.petmilyday.dto.cart.CartItemResponseDto;
import com.petmilyday.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal; // 💡 시큐리티 Principal 임포트 추가
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;

    // 장바구니 페이지 이동 및 등록된 상품 목록 조회
    @GetMapping("/cart")
    public String cartPage(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<CartItemResponseDto> cartItems = cartService.getCartItems(principal.getName());

        model.addAttribute("cartItems", cartItems);
        return "cart/cart";
    }
}