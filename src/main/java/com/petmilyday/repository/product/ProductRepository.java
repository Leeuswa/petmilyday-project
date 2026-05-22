package com.petmilyday.repository.product;

import com.petmilyday.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository를 상속받았기에 기본 CRUD가 자동 생성
}