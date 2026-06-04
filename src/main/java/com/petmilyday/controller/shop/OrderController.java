package com.petmilyday.controller.shop;

import com.petmilyday.dto.shop.OrderResponseDto;
import com.petmilyday.service.shop.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 결제 페이지 이동
     */
    @GetMapping("/checkout")
    public String checkoutPage() {
        return "shop/checkout";
    }

    /**
     * 사용자의 주문 내역 페이지 이동
     */
    @GetMapping("/order_history")
    public String orderHistoryPage(Model model, Principal principal) {
        if (principal != null) {
            List<OrderResponseDto> historyList = orderService.getOrderHistory(principal.getName());
            model.addAttribute("orderHistoryList", historyList);
        }
        return "shop/order_history";
    }
}