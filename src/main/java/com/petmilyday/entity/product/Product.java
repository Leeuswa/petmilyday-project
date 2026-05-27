package com.petmilyday.entity.product;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "img_url", nullable = false, length = 255)
    private String imgUrl;

    @Column(name = "pet_species", length = 50)
    private String petSpecies;

    // =============== [상세페이지 연동 컬럼 추가] ===============
    @Column(name = "material", length = 100)
    private String material; // 주요 소재 (예: 마이크로화이버 고밀도 코튼)

    @Column(name = "size_info", length = 100)
    private String sizeInfo; // 사이즈 (예: M size)

    @Column(name = "origin", length = 100)
    private String origin; // 제조국 / 제조원 (예: 대한민국 / petmilyday 협력사)
    // ========================================================

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}