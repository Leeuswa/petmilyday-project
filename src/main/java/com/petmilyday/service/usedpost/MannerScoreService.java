package com.petmilyday.service.usedpost;

public interface MannerScoreService {

    void evaluate(
            Long fromMemberId,
            Long toMemberId,
            Long usedPostId,
            Integer score
    );

    Double getAverageScore(Long memberId);
}