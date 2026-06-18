package com.petmilyday.repository.shop;

import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 특정 회원의 구독 상태별 정기구독 목록 조회
    List<Subscription> findByMemberUsernameAndStatus(String username, SubscriptionStatus status);

    List<Subscription> findByProductAndStatus(Product product, SubscriptionStatus status);

    List<Subscription> findByMemberUsernameOrderByCreatedAtDesc(String username);
}