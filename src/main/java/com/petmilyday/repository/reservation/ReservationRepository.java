package com.petmilyday.repository.reservation;

import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    // 내 예약 목록 최신순 조회
    // 일반회원이 본인의 예약 목록을 볼 때 사용
    List<Reservation> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 내 예약 목록 최신순 조회 + 페이징
    Page<Reservation> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 특정 병원, 날짜, 시간의 예약 수 조회
    // 예약 가능 슬롯 계산할 때 사용
    long countByHospitalAndReserveDateAndReserveTimeAndStatusNot(
            Hospital hospital,
            LocalDate reserveDate,
            LocalTime reserveTime,
            ReservationStatus status
    );

    // 같은 회원이 같은 날짜, 같은 시간에 이미 예약했는지 확인
    // 중복 예약 방지용
    boolean existsByMemberAndReserveDateAndReserveTimeAndStatusNot(
            Member member,
            LocalDate reserveDate,
            LocalTime reserveTime,
            ReservationStatus status
    );

    // 현재 병원에서 로그인한 회원이 진료 완료(DONE)한 예약 목록 조회
    // 리뷰 작성 가능한 예약을 찾을 때 사용
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
    // 일반회원이 진료 완료된 예약 기록을 볼 때 사용
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

    // 내 동물 진료기록 조회 + 페이징
    // 일반회원이 진료 완료된 예약 기록을 페이지 단위로 볼 때 사용
    @Query(
            value = """
                    SELECT r
                    FROM Reservation r
                    JOIN FETCH r.hospital h
                    JOIN FETCH r.member m
                    LEFT JOIN FETCH r.pet p
                    WHERE m.username = :username
                      AND r.status = :status
                    ORDER BY r.reserveDate DESC, r.reserveTime DESC
                    """,
            countQuery = """
                    SELECT count(r)
                    FROM Reservation r
                    JOIN r.member m
                    WHERE m.username = :username
                      AND r.status = :status
                    """
    )
    Page<Reservation> findMedicalRecordsPage(
            @Param("username") String username,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );

    // 병원 관리자용 예약 목록 조회
    // 담당 병원의 전체 예약을 날짜/시간 최신순으로 조회
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.hospital.id = :hospitalId
            ORDER BY r.reserveDate DESC, r.reserveTime DESC
            """)
    List<Reservation> findHospitalReservations(
            @Param("hospitalId") Long hospitalId
    );

    // 병원 관리자용 예약 목록 페이징 조회
    // 담당 병원의 전체 예약을 페이지 단위로 조회
    // 실시간 대기열 재정렬용 예약 목록 조회
    // 같은 병원 + 같은 날짜 + 같은 시간대 예약 중 취소 상태가 아닌 예약들을 생성순으로 가져옴
    // 생성순으로 1번, 2번, 3번 대기번호를 다시 부여하기 위해 사용
    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.member
            WHERE r.hospital.id = :hospitalId
              AND r.reserveDate = :reserveDate
              AND r.reserveTime = :reserveTime
              AND r.status <> :status
            ORDER BY r.createdAt ASC
            """)
    List<Reservation> findActiveReservationsForWaitingQueue(
            @Param("hospitalId") Long hospitalId,
            @Param("reserveDate") LocalDate reserveDate,
            @Param("reserveTime") LocalTime reserveTime,
            @Param("status") ReservationStatus status
    );
}