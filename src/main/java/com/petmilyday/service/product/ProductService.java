package com.petmilyday.service.product;

import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.dto.product.ProductRequestDto;
import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.shop.SubscriptionRepository;
import com.petmilyday.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class ProductService {

    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final S3UploadService s3UploadService;
    private final NotificationService notificationService;

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findByIsDeletedFalse().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndIsDeletedFalse(category).stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));
        return new ProductResponseDto(product);
    }

    public List<Product> findAll() {
        return productRepository.findByIsDeletedFalse();
    }

    @Transactional
    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));

        product.setDeleted(true);

        List<Subscription> activeSubscriptions = subscriptionRepository.findByProductAndStatus(product, SubscriptionStatus.ACTIVE);

        for (Subscription sub : activeSubscriptions) {
            sub.setStatus(SubscriptionStatus.CANCELLED);
            notifySubscriptionCancelled(sub, product);
        }
    }

    private void notifySubscriptionCancelled(Subscription subscription, Product product) {
        try {
            notificationService.sendToUser(
                    subscription.getMember().getUsername(),
                    NotificationDTO.builder()
                            .type("SUBSCRIPTION_PRODUCT_DISCONTINUED")
                            .message(product.getName() + " 상품이 판매종료되어 정기구독이 취소되었습니다.")
                            .url("/shop/subscription")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("정기구독 판매종료 알림 전송 실패 - subscriptionId: {}, member: {}",
                    subscription.getId(), subscription.getMember().getUsername(), e);
        }
    }

    @Transactional
    public void registerProduct(ProductRequestDto dto) {
        String imgUrl = s3UploadService.uploadFile(dto.getImageFile());

        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .imgUrl(imgUrl)
                .petSpecies(dto.getPetSpecies())
                .material(dto.getMaterial())
                .sizeInfo(dto.getSizeInfo())
                .origin(dto.getOrigin())
                .isActive(true)
                .isDeleted(false)
                .build();

        productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));
    }

    @Transactional
    public void updateProduct(Long id, ProductRequestDto dto, MultipartFile file, MultipartFile descFile) {
        Product product = findById(id);

        System.out.println("=========================================");
        System.out.println("현재 수정할 상품 ID: " + id);
        if (file == null) {
            System.out.println("넘어온 MultipartFile 객체 자체가 null");
        } else {
            System.out.println("파일 파라미터명: " + file.getName());
            System.out.println("실제 파일명: " + file.getOriginalFilename());
            System.out.println("파일 크기: " + file.getSize() + " bytes");
            System.out.println("파일 비어있음 여부(isEmpty): " + file.isEmpty());
        }

        if (file != null && !file.isEmpty()) {
            System.out.println("file.isEmpty()가 false이므로 S3 업로드를 시도합니다.");

            String newImgUrl = s3UploadService.uploadFile(file);

            System.out.println("S3에서 새로 발급해준 이미지 URL: " + newImgUrl);

            product.setImgUrl(newImgUrl);
        } else {
            System.out.println("파일이 비어있거나 null이라서 S3 업로드 로직을 건너뜁니다. (기존 주소 유지)");
        }
        System.out.println("=========================================");

        if (descFile != null && !descFile.isEmpty()) {
            String newDescImgUrl = s3UploadService.uploadFile(descFile);
            product.setDescImgUrl(newDescImgUrl);
        }

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setPetSpecies(dto.getPetSpecies());
        product.setDescription(dto.getDescription());
        product.setMaterial(dto.getMaterial());
        product.setSizeInfo(dto.getSizeInfo());
        product.setOrigin(dto.getOrigin());
    }

    public Page<Product> getAdminProductPage(int page, int size, String sortType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        if ("priceAsc".equals(sortType)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDesc".equals(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("nameAsc".equals(sortType)) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByIsDeletedFalse(pageable);
    }
}