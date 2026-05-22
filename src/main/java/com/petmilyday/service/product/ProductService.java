package com.petmilyday.service.product;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }
}