package com.petmilyday.repository.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepositoryCustom {

    // 슬롯 중복 체크 (취소 제외한 예약 수)
    long countAvailableSlot(Long hospitalId, LocalDate reserveDate, LocalTime reserveTime);
}