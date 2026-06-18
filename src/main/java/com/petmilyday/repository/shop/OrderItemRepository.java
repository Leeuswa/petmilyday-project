package com.petmilyday.repository.shop;

import com.petmilyday.entity.shop.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

// 주문 상품 상세 항목 엔티티에 대한 기본 CRUD 기본 레포지토리
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}