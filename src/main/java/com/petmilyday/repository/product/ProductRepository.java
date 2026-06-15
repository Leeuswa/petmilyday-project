package com.petmilyday.repository.product;

import com.petmilyday.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 특정 카테고리 기준 상품 목록 조회
    List<Product> findByCategory(String category);
}