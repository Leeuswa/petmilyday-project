package com.petmilyday.repository.used;

import com.petmilyday.entity.used.UsedPostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsedPostReportRepository
        extends JpaRepository<UsedPostReport, Long> {

    boolean existsByUsedPost_IdAndMember_Id(
            Long postId,
            Long memberId
    );

    // 메인 관리자 - 게시글 신고 목록 + 처리상태(숨김여부)/키워드(게시글제목·신고자·사유) 검색 + 페이징
    @Query(
            value = """
                select r
                from UsedPostReport r
                join fetch r.usedPost p
                left join fetch r.member m
                where (:hidden is null or p.isHidden = :hidden)
                  and (:keyword is null or :keyword = ''
                       or p.title like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or r.reason like concat('%', :keyword, '%'))
                order by r.createdAt desc
            """,
            countQuery = """
                select count(r)
                from UsedPostReport r
                join r.usedPost p
                left join r.member m
                where (:hidden is null or p.isHidden = :hidden)
                  and (:keyword is null or :keyword = ''
                       or p.title like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or r.reason like concat('%', :keyword, '%'))
            """
    )
    Page<UsedPostReport> searchForAdmin(
            @Param("hidden") Boolean hidden,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}