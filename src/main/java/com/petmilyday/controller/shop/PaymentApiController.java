package com.petmilyday.controller.shop;

import com.petmilyday.entity.shop.Orders;
import com.petmilyday.entity.shop.OrderItem;
import com.petmilyday.entity.product.Product;
import com.petmilyday.repository.cart.CartItemRepository;
import com.petmilyday.repository.cart.CartRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import com.petmilyday.repository.shop.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentApiController {
    private final MemberRepository memberRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

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

            String mainAddress = params.get("deliveryAddress") != null ? params.get("deliveryAddress").toString() : "";
            String detailAddress = params.get("detailAddress") != null ? params.get("detailAddress").toString() : "";
            String fullAddress = mainAddress;
            if (!detailAddress.isEmpty()) {
                fullAddress += " " + detailAddress;
            }
            
            session.setAttribute("receiverName", params.get("receiverName"));
            session.setAttribute("receiverPhone", params.get("receiverPhone"));
            session.setAttribute("deliveryAddress", fullAddress);
            session.setAttribute("totalPrice", totalAmount);
            session.setAttribute("itemName", itemName);

            if (params.get("items") != null) {
                session.setAttribute("checkoutItems", params.get("items"));
                session.setAttribute("isCart", "Y");
            } else {
                session.setAttribute("productId", params.get("productId"));
                session.setAttribute("quantity", params.get("quantity"));
                session.setAttribute("price", params.get("price"));
                session.setAttribute("isCart", "N");
            }

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
    public ModelAndView paymentSuccess(@RequestParam("pg_token") String pgToken,
                                       HttpSession session,
                                       java.security.Principal principal) {
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");
        if (totalPrice == null) {
            totalPrice = 14000;
        }

        try {
            String address = (String) session.getAttribute("deliveryAddress");
            String itemName = (String) session.getAttribute("itemName");
            String isCart = (String) session.getAttribute("isCart");

            String receiverName = (String) session.getAttribute("receiverName");
            String receiverPhone = (String) session.getAttribute("receiverPhone");

            if (principal == null) {
                throw new IllegalStateException("로그인 세션이 만료되었습니다. 다시 시도해 주세요.");
            }

            String username = principal.getName();

            com.petmilyday.entity.member.Member orderMember = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            Orders newOrder = Orders.builder()
                    .member(orderMember)
                    .orderName(itemName != null ? itemName : "반려동물 용품")
                    .totalPrice(totalPrice)
                    .deliveryAddress(address != null ? address : "배송지 미입력")
                    .receiverName(receiverName != null ? receiverName : "수령인 미입력")
                    .receiverPhone(receiverPhone != null ? receiverPhone : "연락처 미입력")
                    .status("PAID")
                    .paymentMethod("KAKAO_PAY")
                    .paymentKey(pgToken)
                    .createdAt(LocalDateTime.now())
                    .build();

            ordersRepository.save(newOrder);

            if ("Y".equals(isCart)) {
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) session.getAttribute("checkoutItems");
                if (cartItems != null) {
                    for (Map<String, Object> item : cartItems) {
                        Object pIdObj = item.get("id") != null ? item.get("id") : item.get("productId");
                        if (pIdObj == null) continue;

                        Long pId = Long.valueOf(pIdObj.toString());
                        int qty = Integer.parseInt(item.get("quantity").toString());
                        int prc = Integer.parseInt(item.get("price").toString());

                        Product productRef = em.getReference(Product.class, pId);

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrders(newOrder);
                        orderItem.setProduct(productRef);
                        orderItem.setQuantity(qty);
                        orderItem.setPrice(prc);

                        orderItemRepository.save(orderItem);
                    }

                    try {
                        com.petmilyday.entity.cart.Cart userCart = cartRepository.findByUserId(orderMember.getId())
                                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

                        Long usersCartId = userCart.getId();
                        cartItemRepository.deleteAllByCartId(usersCartId);
                        System.out.println("결제 완료: 해당 유저의 장바구니 아이템 전체 삭제 성공!");
                    } catch (Exception e) {
                        System.err.println("장바구니 비우기 실패: " + e.getMessage());
                    }
                }
            } else {
                Object pIdObj = session.getAttribute("productId");
                if (pIdObj != null) {
                    Long productId = Long.valueOf(pIdObj.toString());
                    int quantity = session.getAttribute("quantity") != null ? Integer.parseInt(session.getAttribute("quantity").toString()) : 1;
                    int price = session.getAttribute("price") != null ? Integer.parseInt(session.getAttribute("price").toString()) : totalPrice;

                    Product productRef = em.getReference(Product.class, productId);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrders(newOrder);
                    orderItem.setProduct(productRef);
                    orderItem.setQuantity(quantity);
                    orderItem.setPrice(price);

                    orderItemRepository.save(orderItem);
                }
            }

            session.removeAttribute("receiverName");
            session.removeAttribute("receiverPhone");
            session.removeAttribute("deliveryAddress");
            session.removeAttribute("totalPrice");
            session.removeAttribute("itemName");
            session.removeAttribute("checkoutItems");
            session.removeAttribute("productId");
            session.removeAttribute("quantity");
            session.removeAttribute("price");
            session.removeAttribute("isCart");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ModelAndView mav = new ModelAndView();
        mav.addObject("totalPrice", totalPrice);
        mav.setViewName("shop/order_success");
        return mav;
    }
}