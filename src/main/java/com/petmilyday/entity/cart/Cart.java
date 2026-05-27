package com.petmilyday.entity.cart;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 장바구니 고유 번호 (자동 생성)

    @Column(name = "user_id", nullable = false)
    private Long userId; // 회원 ID (에러 났던 바로 그 부분!)

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart(Long userId) {
        this.userId = userId;
    }
}