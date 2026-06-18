package com.petmilyday.service.product;

import com.petmilyday.dto.product.ProductRequestDto;
import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.shop.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final S3UploadService s3UploadService;

    // 전체 상품 조회 (삭제 제외)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findByIsDeletedFalse().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 상품 조회 (삭제 제외)
    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndIsDeletedFalse(category).stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 상품 상세 조회
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));
        return new ProductResponseDto(product);
    }

    // 관리자용 전체 상품 목록 조회 (삭제 제외)
    public List<Product> findAll() {
        return productRepository.findByIsDeletedFalse();
    }

    // 상품 논리 삭제 및 구독 자동 취소
    @Transactional
    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));

        product.setDeleted(true);

        // 해당 상품을 구독 중인 활성 구독자 조회
        List<Subscription> activeSubscriptions = subscriptionRepository.findByProductAndStatus(product, SubscriptionStatus.ACTIVE);

        // 구독 취소 (알림 로직 제거)
        for (Subscription sub : activeSubscriptions) {
            sub.setStatus(SubscriptionStatus.CANCELLED);
        }
    }
    @Transactional
    public void registerProduct(ProductRequestDto dto) {
        // 1. S3에 이미지 업로드하고 URL 받기
        String imgUrl = s3UploadService.uploadFile(dto.getImageFile());

        // 2. 엔티티 생성
        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .imgUrl(imgUrl) // S3에서 받은 URL 저장!
                .petSpecies(dto.getPetSpecies())
                .material(dto.getMaterial())
                .sizeInfo(dto.getSizeInfo())
                .origin(dto.getOrigin())
                .isActive(true)
                .isDeleted(false)
                .build();

        // 3. DB 저장
        productRepository.save(product);
    }

}