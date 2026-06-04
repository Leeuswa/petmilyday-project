package com.petmilyday.repository.reservation;

import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            @Param("status") String status
    );

    //내 동물 진료기록 조회
    @Query("""
    select r
    from  Reservation  r
    join r.member m 
    where m.username = :username
    and r.status = :status
    order by  r.reserveDate desc , r.reserveTime desc 
""")
    List<Reservation> findMedicalRecords(
            @Param("username") String username,
            @Param("status") String status
    );

}
