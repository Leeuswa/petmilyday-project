package com.petmilyday.repository.product;

import com.petmilyday.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByIsDeletedFalse();

    List<Product> findByCategoryAndIsDeletedFalse(String category);

    Page<Product> findByIsDeletedFalse(Pageable pageable);
}