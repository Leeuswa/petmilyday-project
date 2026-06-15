package com.petmilyday.repository.reservation;

import com.petmilyday.entity.reservation.QReservation;

import com.petmilyday.entity.reservation.ReservationStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public long countAvailableSlot(Long hospitalId, LocalDate reserveDate, LocalTime reserveTime) {
        QReservation reservation = QReservation.reservation;

        Long count = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        reservation.hospital.id.eq(hospitalId),
                        reservation.reserveDate.eq(reserveDate),
                        reservation.reserveTime.eq(reserveTime),
                        reservation.status.ne(ReservationStatus.CANCEL)
                )
                .fetchOne();
        return count != null ? count : 0L;

    }
}
