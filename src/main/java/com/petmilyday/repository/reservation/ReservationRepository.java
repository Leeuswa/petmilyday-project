package com.petmilyday.repository.reservation;

import com.petmilyday.entity.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> ,ReservationRepositoryCustom{
    // 내 예약 목록 최신순
    List<Reservation> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
