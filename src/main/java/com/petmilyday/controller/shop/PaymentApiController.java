package com.petmilyday.controller.shop;

import com.petmilyday.entity.shop.Orders;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentApiController {
    private final MemberRepository memberRepository;
    private final OrdersRepository ordersRepository;

    @PostMapping("/ready")
    public ResponseEntity<?> readyStandardPayment(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SECRET_KEY DEV84178147170D9A889C5E6A7155387119098D0");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("cid", "TC0ONETIME");
            body.put("partner_order_id", "petmily_order_1");
            body.put("partner_user_id", "petmily_user_1");

            String itemName = (String) params.get("itemName");
            if (itemName == null || itemName.isEmpty()) {
                itemName = "반려동물 용품 간식";
            }
            body.put("item_name", itemName);
            body.put("quantity", 1);

            Object totalAmountObj = params.get("totalAmount");
            int totalAmount = 1000;
            if (totalAmountObj instanceof Number) {
                totalAmount = ((Number) totalAmountObj).intValue();
            }
            body.put("total_amount", totalAmount);
            body.put("tax_free_amount", 0);

            body.put("approval_url", "http://localhost:8080/api/payment/success");
            body.put("cancel_url", "http://localhost:8080/api/payment/cancel");
            body.put("fail_url", "http://localhost:8080/api/payment/fail");

            // DB 저장을 위해 프론트 전송 값 세션 임시 보관
            session.setAttribute("receiverName", params.get("receiverName"));
            session.setAttribute("deliveryAddress", params.get("deliveryAddress"));
            session.setAttribute("totalPrice", totalAmount);
            session.setAttribute("itemName", itemName);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/ready", request, Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public ModelAndView paymentSuccess(@RequestParam("pg_token") String pgToken, HttpSession session) {
        // 1. 변수를 메서드 최상단에 선언해서 세션 값 안전하게 백업
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");
        if (totalPrice == null) {
            totalPrice = 14000; // 혹시 모를 세션 유실 대비 기본값 방어막 (네 상품 금액에 맞게 세팅)
        }

        try {
            String address = (String) session.getAttribute("deliveryAddress");
            String itemName = (String) session.getAttribute("itemName");
            Long memberId = (Long) session.getAttribute("memberId");

            if (memberId == null) {
                memberId = 1L;
            }

            com.petmilyday.entity.member.Member orderMember = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            Orders newOrder = Orders.builder()
                    .member(orderMember)
                    .orderName(itemName != null ? itemName : "반려동물 용품")
                    .totalPrice(totalPrice)
                    .deliveryAddress(address != null ? address : "배송지 미입력")
                    .status("PAID")
                    .paymentMethod("KAKAO_PAY")
                    .paymentKey(pgToken)
                    .createdAt(LocalDateTime.now())
                    .build();

            ordersRepository.save(newOrder);

            // 2. DB 저장 다 끝난 다음에 세션 지우기! (totalPrice는 위에서 이미 백업 완료)
            session.removeAttribute("receiverName");
            session.removeAttribute("deliveryAddress");
            session.removeAttribute("totalPrice");
            session.removeAttribute("itemName");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 백업해둔 진짜 금액 데이터를 화면단으로 정확하게 토스!
        ModelAndView mav = new ModelAndView();
        mav.addObject("totalPrice", totalPrice);
        mav.setViewName("shop/order_success");
        return mav;
    }
}