package com.petmilyday.repository.reservation;

import com.petmilyday.dto.admin.ReservationSearchDTO;
import com.petmilyday.entity.reservation.QReservation;

import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @Override
    public Page<Reservation> searchAdminReservationsPage(ReservationSearchDTO searchDTO, Pageable pageable) {
        QReservation reservation = QReservation.reservation;

        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .join(reservation.hospital).fetchJoin()
                .join(reservation.member).fetchJoin()
                .leftJoin(reservation.pet).fetchJoin()
                .where(
                        hospitalNameContains(searchDTO.getHospitalName()),
                        statusEq(searchDTO.getStatus()),
                        dateGoe(searchDTO.getDateFrom()),
                        dateLoe(searchDTO.getDateTo())
                )
                .orderBy(
                        reservation.reserveDate.desc(),
                        reservation.reserveTime.desc(),
                        reservation.createdAt.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        hospitalNameContains(searchDTO.getHospitalName()),
                        statusEq(searchDTO.getStatus()),
                        dateGoe(searchDTO.getDateFrom()),
                        dateLoe(searchDTO.getDateTo())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Reservation> searchHospitalReservationsPage(
            Long hospitalId,
            ReservationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    ) {
        QReservation reservation = QReservation.reservation;

        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .join(reservation.hospital).fetchJoin()
                .join(reservation.member).fetchJoin()
                .leftJoin(reservation.pet).fetchJoin()
                .where(
                        reservation.hospital.id.eq(hospitalId),
                        statusEq(status),
                        dateGoe(dateFrom),
                        dateLoe(dateTo)
                )
                .orderBy(
                        reservation.reserveDate.desc(),
                        reservation.reserveTime.desc(),
                        reservation.createdAt.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        reservation.hospital.id.eq(hospitalId),
                        statusEq(status),
                        dateGoe(dateFrom),
                        dateLoe(dateTo)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression hospitalNameContains(String hospitalName) {
        if (hospitalName == null || hospitalName.isBlank()) {
            return null;
        }

        return QReservation.reservation.hospital.name.containsIgnoreCase(hospitalName);
    }

    private BooleanExpression statusEq(ReservationStatus status) {
        if (status == null) {
            return null;
        }

        return QReservation.reservation.status.eq(status);
    }

    private BooleanExpression dateGoe(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }

        return QReservation.reservation.reserveDate.goe(dateFrom);
    }

    private BooleanExpression dateLoe(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }

        return QReservation.reservation.reserveDate.loe(dateTo);
    }
}
