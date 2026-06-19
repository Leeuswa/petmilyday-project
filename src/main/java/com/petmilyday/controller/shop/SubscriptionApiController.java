package com.petmilyday.controller.shop;

import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import com.petmilyday.entity.shop.Orders;
import com.petmilyday.entity.shop.OrderItem;
import com.petmilyday.entity.product.Product;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.shop.SubscriptionRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import com.petmilyday.repository.shop.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
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
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    @PostMapping("/ready")
    public ResponseEntity<?> readySubscription(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SECRET_KEY DEV84178147170D9A889C5E6A7155387119098D0");
            headers.setContentType(MediaType.APPLICATION_JSON);

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

            String mainAddress = params.get("deliveryAddress") != null ? params.get("deliveryAddress").toString() : "";
            String detailAddress = params.get("detailAddress") != null ? params.get("detailAddress").toString() : "";
            String fullAddress = mainAddress;
            if (!detailAddress.isEmpty()) {
                fullAddress += " " + detailAddress;
            }

            session.setAttribute("receiverName", params.get("receiverName"));
            session.setAttribute("receiverPhone", params.get("receiverPhone"));
            session.setAttribute("deliveryAddress", fullAddress);
            session.setAttribute("deliveryMemo", detailAddress);

            session.setAttribute("totalPrice", totalAmount);
            session.setAttribute("itemName", itemName);
            session.setAttribute("productId", params.get("productId") != null ? ((Number) params.get("productId")).longValue() : 1L);
            session.setAttribute("cycleDays", params.get("cycleDays") != null ? ((Number) params.get("cycleDays")).intValue() : 30);
            session.setAttribute("quantity", params.get("quantity") != null ? ((Number) params.get("quantity")).intValue() : 1);
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
    public ModelAndView subscriptionSuccess(@RequestParam("pg_token") String pgToken,
                                            HttpSession session,
                                            java.security.Principal principal) {
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");
        if (totalPrice == null) totalPrice = 9900;

        try {
            String tid = (String) session.getAttribute("tid");
            Long productId = (Long) session.getAttribute("productId");
            Integer cycleDays = (Integer) session.getAttribute("cycleDays");
            Integer quantity = (Integer) session.getAttribute("quantity");
            String itemName = (String) session.getAttribute("itemName");

            String address = (String) session.getAttribute("deliveryAddress");
            String receiverName = (String) session.getAttribute("receiverName");
            String receiverPhone = (String) session.getAttribute("receiverPhone");
            String deliveryMemo = (String) session.getAttribute("deliveryMemo");

            String partnerOrderId = (String) session.getAttribute("partnerOrderId");
            String partnerUserId = (String) session.getAttribute("partnerUserId");

            if (principal == null) {
                throw new IllegalStateException("로그인 세션이 만료되었습니다.");
            }

            if (productId == null) productId = 1L;
            if (cycleDays == null) cycleDays = 30;
            if (quantity == null) quantity = 1;
            if (partnerOrderId == null) partnerOrderId = "petmily_sub_1";
            if (partnerUserId == null) partnerUserId = "petmily_user_1";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "SECRET_KEY DEV84178147170D9A889C5E6A7155387119098D0");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> approveBody = new HashMap<>();
            approveBody.put("cid", "TCSUBSCRIP");
            approveBody.put("tid", tid);
            approveBody.put("partner_order_id", partnerOrderId);
            approveBody.put("partner_user_id", partnerUserId);
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

            String username = principal.getName();
            com.petmilyday.entity.member.Member orderMember = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            com.petmilyday.entity.product.Product subProduct = em.getReference(com.petmilyday.entity.product.Product.class, productId);

            Subscription newSubscription = new Subscription();
            newSubscription.setMember(orderMember);
            newSubscription.setProduct(subProduct);
            newSubscription.setQuantity(quantity);
            newSubscription.setCycleDays(cycleDays);
            newSubscription.setNextDeliveryDate(LocalDate.now().plusDays(cycleDays));
            newSubscription.setBillingKey(sid);
            newSubscription.setCreatedAt(LocalDateTime.now());

            try {
                newSubscription.setStatus(SubscriptionStatus.ACTIVE);
            } catch (Exception e) {
                System.err.println("Enum 설정 실패, 엔티티 검증 필요");
            }

            subscriptionRepository.save(newSubscription);

            int currentRound = 1;
            Orders firstOrder = newSubscription.createOrder(itemName, totalPrice, pgToken, currentRound);

            if (address != null) {
                firstOrder.setDeliveryAddress(address);
            }
            if (receiverName != null) {
                firstOrder.setReceiverName(receiverName);
            }
            if (receiverPhone != null) {
                firstOrder.setReceiverPhone(receiverPhone);
            }

            ordersRepository.save(firstOrder);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrders(firstOrder);
            orderItem.setProduct(subProduct);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(totalPrice);

            orderItemRepository.save(orderItem);

            session.removeAttribute("totalPrice");
            session.removeAttribute("productId");
            session.removeAttribute("cycleDays");
            session.removeAttribute("quantity");
            session.removeAttribute("tid");
            session.removeAttribute("itemName");
            session.removeAttribute("partnerOrderId");
            session.removeAttribute("partnerUserId");
            session.removeAttribute("receiverName");
            session.removeAttribute("receiverPhone");
            session.removeAttribute("deliveryAddress");
            session.removeAttribute("deliveryMemo");

        } catch (Exception e) {
            System.err.println("❌ 정기구독 최종 승인 단계 실패 로그:");
            e.printStackTrace();
        }

        ModelAndView mav = new ModelAndView();
        mav.addObject("totalPrice", totalPrice);
        mav.setViewName("shop/subscription_success");
        return mav;
    }

    @Transactional
    @PostMapping("/cancel")
    public ModelAndView cancelSubscription(@RequestParam("subId") Long subId) {
        try {
            Subscription subscription = subscriptionRepository.findById(subId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 구독 내역이 존재하지 않습니다. ID: " + subId));

            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.saveAndFlush(subscription);
            System.out.println("✅ 구독 해지 완료! 구독 ID: " + subId);

        } catch (Exception e) {
            System.err.println("❌ 구독 해지 실패:");
            e.printStackTrace();
        }

        return new ModelAndView("redirect:/shop/subscription");
    }

    @Transactional
    @PostMapping("/change-cycle")
    public ModelAndView changeSubscriptionCycle(@RequestParam("subId") Long subId,
                                                @RequestParam("cycleDays") int cycleDays) {
        try {
            Subscription subscription = subscriptionRepository.findById(subId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 구독 내역이 존재하지 않습니다. ID: " + subId));

            subscription.setCycleDays(cycleDays);
            subscription.setNextDeliveryDate(LocalDate.now().plusDays(cycleDays));

            subscriptionRepository.saveAndFlush(subscription);
            System.out.println("✅ 구독 주기 변경 완료! 구독 ID: " + subId + " -> 변경된 주기: " + cycleDays + "일");

        } catch (Exception e) {
            System.err.println("❌ 주기 변경 실패:");
            e.printStackTrace();
        }

        return new ModelAndView("redirect:/shop/subscription");
    }
}