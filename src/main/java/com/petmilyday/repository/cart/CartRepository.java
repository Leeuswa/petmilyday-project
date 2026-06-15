package com.petmilyday.repository.cart;

import com.petmilyday.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 특정 회원 ID 기준 장바구니 엔티티 조회
    Optional<Cart> findByUserId(Long userId);
}