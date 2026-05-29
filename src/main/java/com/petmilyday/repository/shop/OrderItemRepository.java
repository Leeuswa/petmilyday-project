package com.petmilyday.repository.shop;

import com.petmilyday.entity.shop.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}