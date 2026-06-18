package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalReviewRepository extends JpaRepository<HospitalReview, Long> {

    // 병원 상세 리뷰 목록 조회
    List<HospitalReview> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);

    // 병원 상세 리뷰 목록 조회 + 페이징
    Page<HospitalReview> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId, Pageable pageable);

    boolean existsByReservation(Reservation reservation);

    // 내가 작성한 병원 리뷰 목록 조회
    @Query("""
        SELECT r
        FROM HospitalReview r
        JOIN r.member m
        WHERE m.username = :username
        ORDER BY r.createdAt DESC
    """)
    List<HospitalReview> findMyReviews(
            @Param("username") String username
    );

    // 내가 작성한 병원 리뷰 목록 조회 + 페이징
    @Query(
            value = """
                SELECT r
                FROM HospitalReview r
                JOIN FETCH r.hospital h
                JOIN FETCH r.member m
                WHERE m.username = :username
                ORDER BY r.createdAt DESC
            """,
            countQuery = """
                SELECT count(r)
                FROM HospitalReview r
                JOIN r.member m
                WHERE m.username = :username
            """
    )
    Page<HospitalReview> findMyReviewsPage(
            @Param("username") String username,
            Pageable pageable
    );

    // 메인 어드민 - 신고된 병원 리뷰 목록 조회
    @Query(
            value = """
            SELECT r
            FROM HospitalReview r
            JOIN FETCH r.hospital h
            JOIN FETCH r.member m
            WHERE r.isReported = true
            ORDER BY r.createdAt DESC
        """,
            countQuery = """
            SELECT count(r)
            FROM HospitalReview r
            WHERE r.isReported = true
        """
    )
    Page<HospitalReview> findReportedReviewsForAdmin(Pageable pageable);

    // 메인 어드민 - 전체 병원 리뷰 목록
    @Query("""
        SELECT r
        FROM HospitalReview r
        JOIN FETCH r.hospital h
        JOIN FETCH r.member m
        ORDER BY r.createdAt DESC
    """)
    List<HospitalReview> findAllReviewsForAdmin();
}