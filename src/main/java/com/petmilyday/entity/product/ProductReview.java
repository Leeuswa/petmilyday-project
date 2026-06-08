package com.petmilyday.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_review")
@Getter @Setter
@NoArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 1. 기본키 (리뷰 식별 고유값)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 2. 회원 ID (리뷰 작성자 식별)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 3. 상품 ID (어느 상품 리뷰인지 연결)

    @Column(name = "order_id", nullable = false)
    private Long orderId; // 4. 주문 ID (실제 구매 확인용, status=DONE 건에만 리뷰 허용 가드)

    @Column(nullable = false)
    private Integer rating; // 5. 평점 (1~5점)

    @Column(columnDefinition = "TEXT")
    private String content; // 6. 리뷰 내용 (미입력 허용, 평점만으로도 가능)

    @Column(name = "img_url", length = 255)
    private String imgUrl; // 7. 리뷰 사진 URL (S3 업로드 후 자동)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 8. 작성일시 (시스템 자동)

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 데이터 적재 시 현재 시간 자동 주입
    }
}