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
import org.springframework.web.multipart.MultipartFile;

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

    // 상품 등록 처리
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

    // 관리자 상품 수정용 단건 엔티티 조회
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));
    }

    // 상품 정보 수정 처리 (더티 체킹 및 S3 이미지 파일 처리 보정)
    @Transactional
    public void updateProduct(Long id, ProductRequestDto dto, MultipartFile file) {
        Product product = findById(id);

        // [디버깅 프린트 1] 컨트롤러에서 파일이 넘어왔는지 검사
        System.out.println("=========================================");
        System.out.println("🚩 [수정 디버깅] 현재 수정할 상품 ID: " + id);
        if (file == null) {
            System.out.println("🚨 [경고] 넘어온 MultipartFile 객체 자체가 null이야!");
        } else {
            System.out.println("📂 [확인] 파일 파라미터명: " + file.getName());
            System.out.println("📂 [확인] 실제 파일명: " + file.getOriginalFilename());
            System.out.println("📂 [확인] 파일 크기: " + file.getSize() + " bytes");
            System.out.println("📂 [확인] 파일 비어있음 여부(isEmpty): " + file.isEmpty());
        }

        if (file != null && !file.isEmpty()) {
            System.out.println("🚀 [진입] file.isEmpty()가 false이므로 S3 업로드를 시도합니다.");

            String newImgUrl = s3UploadService.uploadFile(file);

            //  [디버깅 프린트 2] S3가 준 결과값 확인
            System.out.println("✨ [완료] S3에서 새로 발급해준 이미지 URL: " + newImgUrl);

            product.setImgUrl(newImgUrl);
        } else {
            System.out.println("🛑 [패스] 파일이 비어있거나 null이라서 S3 업로드 로직을 건너뜁니다. (기존 주소 유지)");
        }
        System.out.println("=========================================");

        // 나머지 일반 정보 변경 반영
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

}