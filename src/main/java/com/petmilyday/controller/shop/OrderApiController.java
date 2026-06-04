package com.petmilyday.controller.shop;

import com.petmilyday.dto.shop.OrderRequestDto;
import com.petmilyday.service.shop.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto requestDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            Long orderId = orderService.createOrder(requestDto, principal.getName());
            return ResponseEntity.ok().body("주문이 성공적으로 접수되었습니다. 주문 ID: " + orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}