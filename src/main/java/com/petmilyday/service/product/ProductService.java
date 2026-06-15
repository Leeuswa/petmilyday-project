package com.petmilyday.service.product;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.entity.product.Product;
import com.petmilyday.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 등록된 전체 상품 목록 조회 및 DTO 변환
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 상품 목록 조회 및 DTO 변환
    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 상품 ID 기준 단건 상세 정보 조회 및 DTO 변환
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 없습니다. id=" + id));

        return new ProductResponseDto(product);
    }
}