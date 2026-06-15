package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HospitalManagerRepository extends JpaRepository<HospitalManager, Long> {

    // 메인 관리자가 신청 목록 볼 때
    List<HospitalManager> findByStatus(HospitalManagerStatus status);

    // 메인 관리자가 신청 목록 볼 때 - member, hospital까지 같이 조회
    @Query("""
            select hm
            from HospitalManager hm
            join fetch hm.member
            join fetch hm.hospital
            where hm.status = :status
            order by hm.id desc
            """)
    List<HospitalManager> findByStatusWithMemberAndHospital(
            @Param("status") HospitalManagerStatus status
    );

    // 이 회원이 이 병원의 승인된 관리자인지 확인
    boolean existsByMemberIdAndHospitalIdAndStatus(
            Long memberId,
            Long hospitalId,
            HospitalManagerStatus status
    );

    // 이 회원이 이 병원에 신청한 적 있는지 확인
    boolean existsByMemberIdAndHospitalId(Long memberId, Long hospitalId);

    // 병원 관리자가 담당 병원 조회할 때
    Optional<HospitalManager> findByMemberIdAndStatus(
            Long memberId,
            HospitalManagerStatus status
    );

    // 특정 병원에 승인된 병원 관리자 목록 조회
    // 예약 신청 시 해당 병원 관리자들에게 SSE 알림을 보내기 위해 사용
    @Query("""
            select hm
            from HospitalManager hm
            join fetch hm.member
            join fetch hm.hospital
            where hm.hospital.id = :hospitalId
              and hm.status = :status
            """)
    List<HospitalManager> findManagersByHospitalIdAndStatus(
            @Param("hospitalId") Long hospitalId,
            @Param("status") HospitalManagerStatus status
    );

    // 이 회원이 이 병원에 신청한 기존 기록 조회
    Optional<HospitalManager> findByMemberIdAndHospitalId(Long memberId, Long hospitalId);
}