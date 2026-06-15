package com.petmilyday.repository.reservation;

import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    // 내 예약 목록 최신순
    List<Reservation> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 특정 시간 예약 수 조회
    long countByHospitalAndReserveDateAndReserveTimeAndStatusNot(
            Hospital hospital,
            LocalDate reserveDate,
            LocalTime reserveTime,
            ReservationStatus status
    );

    // 같은 시간 중복 예약 체크
    boolean existsByMemberAndReserveDateAndReserveTimeAndStatusNot(
            Member member,
            LocalDate reserveDate,
            LocalTime reserveTime,
            ReservationStatus status
    );

    // 현재 병원에서 로그인한 회원이 진료 완료(DONE)한 예약 목록 조회
    @Query("""
            SELECT r
            FROM Reservation r
            JOIN r.hospital h
            JOIN r.member m
            WHERE h.id = :hospitalId
              AND m.username = :username
              AND r.status = :status
            ORDER BY r.createdAt DESC
            """)
    List<Reservation> findReviewableReservations(
            @Param("hospitalId") Long hospitalId,
            @Param("username") String username,
            @Param("status") ReservationStatus status
    );

    // 내 동물 진료기록 조회
    @Query("""
            SELECT r
            FROM Reservation r
            JOIN r.member m
            WHERE m.username = :username
              AND r.status = :status
            ORDER BY r.reserveDate DESC, r.reserveTime DESC
            """)
    List<Reservation> findMedicalRecords(
            @Param("username") String username,
            @Param("status") ReservationStatus status
    );

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.hospital.id = :hospitalId
        ORDER BY r.reserveDate DESC, r.reserveTime DESC
        """)
    List<Reservation> findHospitalReservations(
            @Param("hospitalId") Long hospitalId
    );
}