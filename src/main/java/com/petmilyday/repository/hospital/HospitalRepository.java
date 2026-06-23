package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.Hospital;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> ,HospitalRepositoryCustom {

    // 예약 등록 시 같은 병원에 대한 동시 요청을 직렬화하기 위한 락 조회
    // (같은 시간대 슬롯에 동시 예약이 몰려 정원을 초과하는 것을 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hospital h WHERE h.id = :id")
    Optional<Hospital> findByIdForUpdate(@Param("id") Long id);
}
