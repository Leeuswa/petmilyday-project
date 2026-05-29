package com.petmilyday.controller.shop;

import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus; // enum 위치에 맞게 수정 필요
import com.petmilyday.repository.shop.SubscriptionRepository; // 네 repository 경로 확인
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionApiController {

    private final SubscriptionRepository subscriptionRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    @PostMapping("/ready")
    public ResponseEntity<?> readySubscription(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SECRET_KEY DEV84178147170D9A889C5E6A7155387119098D0"); // 네가 바꾼 진짜 DEV 키 적용
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 프론트에서 가맹점 ID들을 넘겨주지 않으면 기본값으로 방어
            String partnerOrderId = params.get("partnerOrderId") != null ? params.get("partnerOrderId").toString() : "petmily_sub_1";
            String partnerUserId = params.get("partnerUserId") != null ? params.get("partnerUserId").toString() : "petmily_user_1";

            Map<String, Object> body = new HashMap<>();
            body.put("cid", "TCSUBSCRIP");
            body.put("partner_order_id", partnerOrderId);
            body.put("partner_user_id", partnerUserId);

            String itemName = (String) params.get("itemName");
            if (itemName == null || itemName.isEmpty()) itemName = "정기구독 상품";
            body.put("item_name", itemName);
            body.put("quantity", 1);

            Object totalAmountObj = params.get("totalAmount");
            int totalAmount = 9900;
            if (totalAmountObj instanceof Number) {
                totalAmount = ((Number) totalAmountObj).intValue();
            }
            body.put("total_amount", totalAmount);
            body.put("tax_free_amount", 0);

            body.put("approval_url", "http://localhost:8080/api/subscription/success");
            body.put("cancel_url", "http://localhost:8080/api/subscription/cancel");
            body.put("fail_url", "http://localhost:8080/api/subscription/fail");

            // ⭐️ [매우 중요] 파트너 ID 세트까지 세션에 완벽하게 박멸 박제하기
            session.setAttribute("totalPrice", totalAmount);
            session.setAttribute("productId", params.get("productId") != null ? ((Number) params.get("productId")).longValue() : 1L);
            session.setAttribute("cycleDays", params.get("cycleDays") != null ? ((Number) params.get("cycleDays")).intValue() : 30);
            session.setAttribute("quantity", 1);
            session.setAttribute("partnerOrderId", partnerOrderId);
            session.setAttribute("partnerUserId", partnerUserId);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/ready", request, Map.class
            );

            Map<String, Object> resBody = response.getBody();
            if (resBody != null && resBody.get("tid") != null) {
                session.setAttribute("tid", resBody.get("tid").toString());
            }

            return ResponseEntity.ok(resBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public ModelAndView subscriptionSuccess(@RequestParam("pg_token") String pgToken, HttpSession session) {
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");
        if (totalPrice == null) totalPrice = 9900;

        try {
            String tid = (String) session.getAttribute("tid");
            Long productId = (Long) session.getAttribute("productId");
            Integer cycleDays = (Integer) session.getAttribute("cycleDays");
            Integer quantity = (Integer) session.getAttribute("quantity");
            Long memberId = (Long) session.getAttribute("memberId");

            // 세션에서 레디 때 썼던 진짜 파트너 ID 세트 복원하기
            String partnerOrderId = (String) session.getAttribute("partnerOrderId");
            String partnerUserId = (String) session.getAttribute("partnerUserId");

            if (memberId == null) memberId = 1L;
            if (productId == null) productId = 1L;
            if (cycleDays == null) cycleDays = 30;
            if (quantity == null) quantity = 1;
            if (partnerOrderId == null) partnerOrderId = "petmily_sub_1";
            if (partnerUserId == null) partnerUserId = "petmily_user_1";

            // 🔄 카카오페이 최종 승인(Approve) API 호출
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SECRET_KEY DEV84178147170D9A889C5E6A7155387119098D0"); // DEV 키 장착
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> approveBody = new HashMap<>();
            approveBody.put("cid", "TCSUBSCRIP");
            approveBody.put("tid", tid);
            approveBody.put("partner_order_id", partnerOrderId); // ready 때랑 100% 동일한 값 세팅
            approveBody.put("partner_user_id", partnerUserId);   // ready 때랑 100% 동일한 값 세팅
            approveBody.put("pg_token", pgToken);

            HttpEntity<Map<String, Object>> approveRequest = new HttpEntity<>(approveBody, headers);
            ResponseEntity<Map> approveResponse = restTemplate.postForEntity(
                    "https://open-api.kakaopay.com/online/v1/payment/approve", approveRequest, Map.class
            );

            Map<String, Object> approveResBody = approveResponse.getBody();
            String sid = "";
            if (approveResBody != null && approveResBody.get("sid") != null) {
                sid = approveResBody.get("sid").toString();
                System.out.println("🔥 정기결제 빌링키 sid 발급 성공: " + sid);
            }

            // 📦 JPA 엔티티 생성 및 데이터 인서트
            com.petmilyday.entity.member.Member orderMember = em.getReference(com.petmilyday.entity.member.Member.class, memberId);
            com.petmilyday.entity.product.Product subProduct = em.getReference(com.petmilyday.entity.product.Product.class, productId);

            Subscription newSubscription = new Subscription();
            newSubscription.setMember(orderMember);
            newSubscription.setProduct(subProduct);
            newSubscription.setQuantity(quantity);
            newSubscription.setCycleDays(cycleDays);
            newSubscription.setNextDeliveryDate(LocalDate.now().plusDays(cycleDays));
            newSubscription.setBillingKey(sid);

            try {
                newSubscription.setStatus(SubscriptionStatus.ACTIVE);
            } catch (Exception e) {
                System.err.println("Enum 설정 실패, 엔티티 검증 필요");
            }

            subscriptionRepository.save(newSubscription);

            // 세션 정리
            session.removeAttribute("totalPrice");
            session.removeAttribute("productId");
            session.removeAttribute("cycleDays");
            session.removeAttribute("quantity");
            session.removeAttribute("tid");
            session.removeAttribute("partnerOrderId");
            session.removeAttribute("partnerUserId");

        } catch (Exception e) {
            System.err.println("❌ 정기구독 최종 승인 단계 실패 로그:");
            e.printStackTrace();
        }

        ModelAndView mav = new ModelAndView();
        mav.addObject("totalPrice", totalPrice);
        mav.setViewName("shop/subscription_success");
        return mav;
    }
}