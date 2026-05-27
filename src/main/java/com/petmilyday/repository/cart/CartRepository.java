package com.petmilyday.repository.cart;

import com.petmilyday.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 유저 ID로 장바구니 찾기!
    Optional<Cart> findByUserId(Long userId);
}