package com.petmilyday.repository.shop;

import com.petmilyday.entity.shop.Subscription;
import com.petmilyday.entity.shop.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // ❌ 기존 코드: findByMemberEmailAndStatus
    // ⭕ 수정 코드: email 대신 username으로 기둥을 바꿔준다!
    List<Subscription> findByMemberUsernameAndStatus(String username, SubscriptionStatus status);
}