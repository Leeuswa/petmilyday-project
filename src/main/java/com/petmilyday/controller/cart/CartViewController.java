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
        Long mockUserId = 1L;
        List<CartItemResponseDto> cartItems = cartService.getCartItems(mockUserId);

        model.addAttribute("cartItems", cartItems);
        return "cart/cart";
    }
}