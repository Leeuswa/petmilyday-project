package com.petmilyday.repository.hospital;

import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalReviewRepository extends JpaRepository<HospitalReview,Long> {

    List<HospitalReview> findByHospitalIdAndIsReportedFalseOrderByCreatedAtDesc(Long hospitalId);

    boolean existsByReservation(Reservation reservation);

    //내가 작성한 병원 리뷰 목록 조회 (로그인한 회원 기준으로 리뷰 최신순 조회)
    @Query("""
        SELECT r 
        from HospitalReview r
        join  r.member m
        where m.username = :username
        and  r.isReported = false 
        order by  r.createdAt desc 
""")
    List<HospitalReview> findMyReviews(
            @Param("username") String username
    );
}
