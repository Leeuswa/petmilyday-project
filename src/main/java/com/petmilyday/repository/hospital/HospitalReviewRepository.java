package com.petmilyday.repository.hospital;

import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalReviewRepository extends JpaRepository<HospitalReview,Long> {

    List<HospitalReview> findByHospitalIdAndIsReportedFalseOrderByCreatedAtDesc(Long hospitalId);

    boolean existsByReservation(Reservation reservation);
}
