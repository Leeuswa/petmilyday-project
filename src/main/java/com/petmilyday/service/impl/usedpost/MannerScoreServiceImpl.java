package com.petmilyday.service.impl.usedpost;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.MannerScore;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.MannerScoreRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.MannerScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MannerScoreServiceImpl implements MannerScoreService {

    private final MannerScoreRepository mannerScoreRepository;
    private final MemberRepository memberRepository;
    private final UsedPostRepository usedPostRepository;

    @Override
    public Double getAverageScore(Long memberId) {

        List<MannerScore> scores =
                mannerScoreRepository.findByToMember_Id(memberId);

        if (scores.isEmpty()) {
            return 0.0;
        }

        return scores.stream()
                .mapToInt(MannerScore::getScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public void evaluate(
            Long fromMemberId,
            Long toMemberId,
            Long usedPostId,
            Integer score
    ) {

        if (fromMemberId == null
                || toMemberId == null
                || usedPostId == null) {
            throw new RuntimeException("평가 정보가 누락되었습니다.");
        }

        if (fromMemberId.equals(toMemberId)) {
            throw new RuntimeException("자기 자신은 평가할 수 없습니다.");
        }

        if (score == null || score < 1 || score > 5) {
            throw new RuntimeException("점수는 1~5점만 가능합니다.");
        }

        boolean alreadyEvaluated =
                mannerScoreRepository.existsByFromMember_IdAndToMember_IdAndUsedPost_Id(
                        fromMemberId,
                        toMemberId,
                        usedPostId
                );

        if (alreadyEvaluated) {
            throw new RuntimeException("이미 평가한 거래입니다.");
        }

        Member fromMember =
                memberRepository.findById(fromMemberId)
                        .orElseThrow();

        Member toMember =
                memberRepository.findById(toMemberId)
                        .orElseThrow();

        UsedPost usedPost =
                usedPostRepository.findById(usedPostId)
                        .orElseThrow();

        MannerScore mannerScore =
                MannerScore.builder()
                        .fromMember(fromMember)
                        .toMember(toMember)
                        .usedPost(usedPost)
                        .score(score)
                        .createdAt(LocalDateTime.now())
                        .build();

        mannerScoreRepository.save(mannerScore);
    }
}