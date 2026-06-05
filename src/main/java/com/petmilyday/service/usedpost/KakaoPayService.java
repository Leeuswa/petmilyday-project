package com.petmilyday.service.usedpost;

import com.petmilyday.entity.used.UsedPost;

public interface KakaoPayService {

    String ready(Long postId, Long buyerId);

    UsedPost approve(Long postId, String pgToken);
}