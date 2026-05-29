package com.petmilyday.dto.reservation;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSlotDto {

    // 예약 시간
    private LocalTime time;

    // 예약 가능 여부
    private boolean available;

    // 현재 예약 수
    private int currentCount;

    // 최대 예약 가능 수
    private int maxCount;
}

