package com.petmilyday.service.impl.usedpost;

import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoPayServiceImpl implements KakaoPayService {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.approval-url}")
    private String approvalUrl;

    @Value("${kakaopay.cancel-url}")
    private String cancelUrl;

    @Value("${kakaopay.fail-url}")
    private String failUrl;

    private final UsedPostRepository usedPostRepository;

    @Override
    public String ready(Long postId, Long buyerId) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow();

        String tid = UUID.randomUUID().toString();

        post.setPaymentKey(tid);
        post.setBuyerId(buyerId);

        usedPostRepository.save(post);

        return "/payment/success?pg_token=" + tid;
    }

    @Override
    public UsedPost approve(String pgToken) {

        UsedPost post =
                usedPostRepository.findByPaymentKey(pgToken)
                        .orElseThrow();

        post.setStatus(UsedPostStatus.SOLD);

        return usedPostRepository.save(post);
    }
}