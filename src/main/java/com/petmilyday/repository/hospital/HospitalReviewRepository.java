package com.petmilyday.repository.hospital;

import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.entity.hospital.HospitalReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalReviewRepository extends JpaRepository<HospitalReview,Long> {

    List<HospitalReview> findByHospitalIdAndIsReportedFalseOrderByCreatedAtDesc(Long hospitalId);
}
