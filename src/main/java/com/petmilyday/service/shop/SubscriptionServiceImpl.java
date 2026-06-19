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
        return subscriptionRepository.findByMemberUsernameAndStatus(username, SubscriptionStatus.ACTIVE)
                .stream()
                .map(SubscriptionResponseDto::new)
                .collect(Collectors.toList());
    }

    // 💡 인터페이스에 추가한 메서드 구현!
    @Override
    public List<SubscriptionResponseDto> getAllSubscriptions(String username) {
        return subscriptionRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(SubscriptionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createSubscription(SubscriptionRequestDto requestDto, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        LocalDate firstDeliveryDate = LocalDate.now().plusDays(requestDto.getCycleDays());

        Subscription subscription = Subscription.builder()
                .member(member)
                .product(product)
                .quantity(requestDto.getQuantity())
                .cycleDays(requestDto.getCycleDays())
                .nextDeliveryDate(firstDeliveryDate)
                .billingKey("KAKAO_DUMMY_BILLING_KEY_12345")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        return subscriptionRepository.save(subscription).getId();
    }

    @Override
    @Transactional
    public void changeCycle(Long id, int newCycle) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 정보입니다."));

        if (subscription.getProduct().isDeleted()) {
            throw new IllegalArgumentException("판매가 종료된 상품은 주기를 변경할 수 없습니다.");
        }

        subscription.setCycleDays(newCycle);
        subscription.setNextDeliveryDate(LocalDate.now().plusDays(newCycle));
    }

    @Override
    @Transactional
    public void cancelSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독 정보입니다."));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 해지된 구독입니다.");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
    }
}