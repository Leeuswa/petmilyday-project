package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalManagerRepository extends JpaRepository<HospitalManager,Long> {

    //메인 관리자가 신청 목록 볼 때
    List<HospitalManager> findByStatus(HospitalManagerStatus status);

    // 이 회원이 이 병원의 승인된 관리자인지 확인
    boolean existsByMemberIdAndHospitalIdAndStatus(
            Long memberId,
            Long hospitalId,
            HospitalManagerStatus status
    );
    // 이 회원이 이 병원의 승인된 관리자인지 확인
    boolean existsByMemberIdAndHospitalId(Long memberId, Long hospitalId);

    // 병원 관리자가 담당 병원 조회할 때
    Optional<HospitalManager> findByMemberIdAndStatus(
            Long memberId,
            HospitalManagerStatus status
    );
}
