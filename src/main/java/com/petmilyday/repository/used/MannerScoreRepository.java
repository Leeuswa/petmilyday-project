package com.petmilyday.repository.used;

import com.petmilyday.entity.used.MannerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MannerScoreRepository extends JpaRepository<MannerScore, Long> {

    List<MannerScore> findByToMember_Id(Long toMemberId);

    @Query("SELECT COALESCE(AVG(m.score), 0) FROM MannerScore m WHERE m.toMember.id = :toMemberId")
    Double findAverageByToMemberId(@Param("toMemberId") Long toMemberId);

    boolean existsByFromMember_IdAndToMember_IdAndUsedPost_Id(
            Long fromMemberId,
            Long toMemberId,
            Long usedPostId
    );

    @Query("""
    SELECT COALESCE(AVG(m.score), 0)
    FROM MannerScore m
    WHERE m.toMember.id = :memberId
""")
    Double findAverageScoreByMemberId(
            @Param("memberId") Long memberId
    );
}