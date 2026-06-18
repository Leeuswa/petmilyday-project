package com.petmilyday.repository.used;

import com.petmilyday.entity.used.UsedPostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsedPostReportRepository
        extends JpaRepository<UsedPostReport, Long> {

    boolean existsByUsedPost_IdAndMember_Id(
            Long postId,
            Long memberId
    );

    @Query(
            value = """
                select r
                from UsedPostReport r
                join fetch r.usedPost p
                left join fetch r.member m
                order by r.createdAt desc
            """,
            countQuery = """
                select count(r)
                from UsedPostReport r
            """
    )
    Page<UsedPostReport> findAllForAdmin(Pageable pageable);
}