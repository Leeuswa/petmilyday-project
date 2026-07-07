package com.petmilyday.entity.shop;

import com.petmilyday.entity.member.Member;   // 네 member 패키지 주소 자동완성 확인!
import com.petmilyday.entity.product.Product; // 네 product 패키지 주소 자동완성 확인!
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*; // Spring Boot 3 버전 맞춤형 jakarta 임포트
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
@Getter
@NoArgsConstructor
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "cycle_days", nullable = false)
    private int cycleDays;

    @Column(name = "next_delivery_date", nullable = false)
    private LocalDate nextDeliveryDate;

    @Column(name = "billing_key", nullable = false, length = 200)
    private String billingKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Subscription(Member member, Product product, int quantity, int cycleDays,
                        LocalDate nextDeliveryDate, String billingKey, SubscriptionStatus status) {
        this.member = member;
        this.product = product;
        this.quantity = quantity;
        this.cycleDays = cycleDays;
        this.nextDeliveryDate = nextDeliveryDate;
        this.billingKey = billingKey;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void updateNextDeliveryDate() {
        this.nextDeliveryDate = this.nextDeliveryDate.plusDays(this.cycleDays);
    }

    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public com.petmilyday.entity.shop.Orders createOrder(String itemName, int totalPrice, String pgToken, int round) {
        String dynamicOrderName = (itemName != null ? itemName : "정기구독 상품") + " (" + round + "회차 결제)";

        return com.petmilyday.entity.shop.Orders.builder()
                .member(this.member)
                .orderName(dynamicOrderName)
                .totalPrice(totalPrice)
                .deliveryAddress("배송지 미입력(정기구독)")
                .status("PAID")
                .paymentMethod("KAKAO_PAY")
                .paymentKey(pgToken)
                .createdAt(LocalDateTime.now())
                .build();
    }
}