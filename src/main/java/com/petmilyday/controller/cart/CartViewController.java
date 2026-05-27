package com.petmilyday.controller.cart;

import com.petmilyday.dto.cart.CartItemResponseDto;
import com.petmilyday.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;

    @GetMapping("/cart")
    public String cartPage(Model model) {
        Long mockUserId = 1L; // 데모용 임시 유저
        List<CartItemResponseDto> cartItems = cartService.getCartItems(mockUserId);

        // 백엔드에서 찾은 리스트를 화면(HTML)으로 쏴줌!
        model.addAttribute("cartItems", cartItems);
        return "cart/cart";
    }
}