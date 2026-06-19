package com.petmilyday.repository.shop;

import com.petmilyday.entity.shop.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByMemberUsernameOrderByCreatedAtDesc(String username);


    // 로그인한 유저가 특정 상품을 구매했는지, 상태가 PAID인지 검증
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "JOIN oi.orders o " +
            "WHERE o.member.username = :username " +
            "AND oi.product.id = :productId " +
            "AND o.status = 'PAID'")
    boolean existsByUsernameAndProductId(@Param("username") String username, @Param("productId") Long productId);
}