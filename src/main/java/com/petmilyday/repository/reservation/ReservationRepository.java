package com.petmilyday.repository.reservation;

import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> ,ReservationRepositoryCustom{
    // 내 예약 목록 최신순
    List<Reservation> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    // 특정 시간 예약 수 조회
    long countByHospitalAndReserveDateAndReserveTimeAndStatusNot(
            Hospital hospital, LocalDate reserveDate, LocalTime reserveTime, String status );
    // 같은 시간 중복 예약 체크
    boolean existsByMemberAndReserveDateAndReserveTimeAndStatusNot(
            Member member, LocalDate reserveDate, LocalTime reserveTime, String status );
}
