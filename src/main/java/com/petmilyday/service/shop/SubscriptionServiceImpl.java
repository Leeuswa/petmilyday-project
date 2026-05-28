package com.petmilyday.service.shop;

import com.petmilyday.dto.shop.SubscriptionRequestDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.shop.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Override
    public List<SubscriptionResponseDto> getActiveSubscriptions(String username) {
        // 시큐리티 세션 ID(username) 기반으로 ACTIVE 상태인 구독만 싹 긁어오기
        return subscriptionRepository.findByMemberUsernameAndStatus(username, SubscriptionStatus.ACTIVE)
                .stream()
                .map(SubscriptionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createSubscription(SubscriptionRequestDto requestDto, String username) {
        // 1. 로그인한 회원 조회 (username 기둥으로 조회)
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 구독할 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 첫 다음 배송일 계산
        LocalDate firstDeliveryDate = LocalDate.now().plusDays(requestDto.getCycleDays());

        // 4. 엔티티 빌드 (카카오페이 실결제 붙이기 전 더미 빌링키 세팅)
        Subscription subscription = Subscription.builder()
                .member(member)
                .product(product)
                .quantity(requestDto.getQuantity())
                .cycleDays(requestDto.getCycleDays())
                .nextDeliveryDate(firstDeliveryDate)
                .billingKey("KAKAO_DUMMY_BILLING_KEY_12345")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // 5. DB 저장
        return subscriptionRepository.save(subscription).getId();
    }

    // [★추가 구현] 배송 주기 변경 비즈니스 로직
    @Override
    @Transactional
    public void changeCycle(Long id, int newCycle) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 정보입니다."));

        // 주기 변경 및 오늘 날짜 기준 다음 배송일 갱신 (더티 체킹 자동 수정)
        subscription.setCycleDays(newCycle);
        subscription.setNextDeliveryDate(LocalDate.now().plusDays(newCycle));
    }

    // [★추가 구현] 정기구독 해지 비즈니스 로직
    @Override
    @Transactional
    public void cancelSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 정보입니다."));

        // 상태를 CANCELLED로 변경하여 활성화 목록에서 즉시 격리시킴 (더티 체킹 자동 수정)
        subscription.setStatus(SubscriptionStatus.CANCELLED);
    }
}