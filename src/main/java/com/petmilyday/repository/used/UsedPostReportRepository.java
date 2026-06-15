package com.petmilyday.repository.used;

import com.petmilyday.entity.used.UsedPostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsedPostReportRepository
        extends JpaRepository<UsedPostReport, Long> {

    boolean existsByUsedPost_IdAndMember_Id(
            Long postId,
            Long memberId
    );
}