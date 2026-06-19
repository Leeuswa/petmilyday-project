package com.petmilyday.service.impl.usedpost;

import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://open-api.kakaopay.com")
                    .build();

    @Override
    public String ready(Long postId, Long buyerId) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        Map<String, Object> params =
                Map.of(
                        "cid", "TC0ONETIME",
                        "partner_order_id", String.valueOf(postId),
                        "partner_user_id", String.valueOf(buyerId),
                        "item_name", post.getTitle(),
                        "quantity", 1,
                        "total_amount", post.getPrice(),
                        "tax_free_amount", 0,
                        "approval_url", approvalUrl + "?postId=" + postId,
                        "cancel_url", cancelUrl,
                        "fail_url", failUrl
                );

        Map response =
                webClient.post()
                        .uri("/online/v1/payment/ready")
                        .header("Authorization", "SECRET_KEY_DEV " + secretKey.trim())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(params)
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                errorResponse -> errorResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            System.out.println("카카오페이 ready 오류 = " + errorBody);
                                            return Mono.error(
                                                    new RuntimeException("카카오페이 ready 오류: " + errorBody)
                                            );
                                        })
                        )
                        .bodyToMono(Map.class)
                        .block();

        if (response == null) {
            throw new RuntimeException("카카오페이 ready 응답 없음");
        }

        String tid =
                response.get("tid").toString();

        String redirectUrl =
                response.get("next_redirect_pc_url").toString();

        post.setPaymentKey(tid);
        post.setBuyerId(buyerId);

        usedPostRepository.save(post);

        return redirectUrl;
    }

    @Override
    public UsedPost approve(Long postId, String pgToken) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();

        params.add("cid", "TC0ONETIME");
        params.add("tid", post.getPaymentKey());
        params.add("partner_order_id", String.valueOf(post.getId()));
        params.add("partner_user_id", String.valueOf(post.getBuyerId()));
        params.add("pg_token", pgToken);

        webClient.post()
                .uri("/online/v1/payment/approve")
                .header("Authorization", "SECRET_KEY_DEV " + secretKey.trim())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        errorResponse -> errorResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.out.println("카카오페이 approve 오류 = " + errorBody);
                                    return Mono.error(
                                            new RuntimeException("카카오페이 approve 오류: " + errorBody)
                                    );
                                })
                )
                .bodyToMono(Map.class)
                .block();

        post.setStatus(UsedPostStatus.SOLD);

        return usedPostRepository.save(post);
    }
}