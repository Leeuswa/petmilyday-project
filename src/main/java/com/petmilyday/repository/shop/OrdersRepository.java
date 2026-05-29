package com.petmilyday.repository.shop;

import com.petmilyday.entity.shop.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByMemberUsernameOrderByCreatedAtDesc(String username);
}