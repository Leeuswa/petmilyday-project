package com.petmilyday.repository.used;

import com.petmilyday.entity.used.MannerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MannerScoreRepository extends JpaRepository<MannerScore, Long> {

    List<MannerScore> findByToMember_Id(Long toMemberId);

    boolean existsByFromMember_IdAndToMember_IdAndUsedPost_Id(
            Long fromMemberId,
            Long toMemberId,
            Long usedPostId
    );
}