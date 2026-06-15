package com.petmilyday.service.shop;

import com.petmilyday.dto.shop.OrderItemDto;
import com.petmilyday.dto.shop.OrderRequestDto;
import com.petmilyday.dto.shop.OrderResponseDto;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.shop.OrderItem;
import com.petmilyday.entity.shop.Orders;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 회원별 주문 내역 조회

    public List<OrderResponseDto> getOrderHistory(String username) {
        List<Orders> ordersList = ordersRepository.findByMemberUsernameOrderByCreatedAtDesc(username);
        List<OrderResponseDto> responseList = new ArrayList<>();

        for (int i = 0; i < ordersList.size(); i++) {
            Orders orders = ordersList.get(i);

            if (orders.getOrderName() != null && orders.getOrderName().contains("(1회")) {
                continue;
            }

            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(orders.getId());
            dto.setProductName(orders.getOrderName());
            dto.setTotalPrice(orders.getTotalPrice());

            if ("ORDERED".equals(orders.getStatus()) || "PAID".equals(orders.getStatus())) {
                dto.setOrderStatus("결제완료");
            } else {
                dto.setOrderStatus(orders.getStatus());
            }

            dto.setOrderDate(orders.getCreatedAt());

            if (orders.getOrderItems() != null && !orders.getOrderItems().isEmpty()) {
                dto.setQuantity(orders.getOrderItems().get(0).getQuantity());
            } else {
                dto.setQuantity(1);
            }

            responseList.add(dto);
        }

        return responseList;
    }

    // 신규 상품 주문 생성
    @Transactional
    public Long createOrder(OrderRequestDto requestDto, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 존재하지 않습니다.");
        }

        Orders orders = Orders.builder()
                .member(member)
                .deliveryAddress(requestDto.getDeliveryAddress())
                .status("ORDERED")
                .paymentMethod(requestDto.getPaymentMethod())
                .createdAt(LocalDateTime.now())
                .build();

        int calcTotalPrice = 0;
        String firstProductName = "";
        int totalItemCount = requestDto.getItems().size();

        for (int i = 0; i < requestDto.getItems().size(); i++) {
            OrderItemDto itemDto = requestDto.getItems().get(i);

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보가 올바르지 않습니다."));

            if (i == 0) {
                firstProductName = product.getName();
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .price(product.getPrice())
                    .build();

            orders.addOrderItem(orderItem);
            calcTotalPrice += (product.getPrice() * itemDto.getQuantity());
        }

        String finalOrderName = firstProductName;
        if (totalItemCount > 1) {
            finalOrderName += " 외 " + (totalItemCount - 1) + "건";
        }

        orders.setOrderName(finalOrderName);
        orders.setTotalPrice(calcTotalPrice);

        return ordersRepository.save(orders).getId();
    }
}