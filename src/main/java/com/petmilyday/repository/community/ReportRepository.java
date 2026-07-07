package com.petmilyday.repository.community;

import com.petmilyday.entity.community.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 메인 관리자 - 커뮤니티 신고 목록 + 상태/구분/키워드(신고자·사유) 검색 + 페이징
    @Query(
            value = """
                select r
                from Report r
                join fetch r.reporter rep
                where (:status is null or :status = '' or r.status = :status)
                  and (:targetType is null or :targetType = '' or r.targetType = :targetType)
                  and (:keyword is null or :keyword = ''
                       or rep.nickname like concat('%', :keyword, '%')
                       or r.reason like concat('%', :keyword, '%'))
                order by r.status asc, r.id desc
            """,
            countQuery = """
                select count(r)
                from Report r
                join r.reporter rep
                where (:status is null or :status = '' or r.status = :status)
                  and (:targetType is null or :targetType = '' or r.targetType = :targetType)
                  and (:keyword is null or :keyword = ''
                       or rep.nickname like concat('%', :keyword, '%')
                       or r.reason like concat('%', :keyword, '%'))
            """
    )
    Page<Report> searchForAdmin(
            @Param("status") String status,
            @Param("targetType") String targetType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}