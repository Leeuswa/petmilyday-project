package com.petmilyday.controller.shop;

import com.petmilyday.dto.shop.OrderItemDto;
import com.petmilyday.service.product.ProductService; // 네 상품 서비스 경로에 맞게 수정!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    // 일반 주문 결제창 가기 (장바구니 & 바로구매 공용)
    @GetMapping("/checkout")
    public String checkoutPage() {
        // 실제 프로젝트에선 세션이나 로컬스토리지, 혹은 쿼리스트링 데이터를 받아 처리함
        return "shop/checkout"; // templates/shop/checkout.html 생성 필요!
    }
}