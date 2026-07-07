package com.petmilyday.dto.reservation;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    private Long hospitalId; //어느 병원에 갈지
    private Long petId; //어느 반려동물인지
    private LocalDate reserveDate; //예약시간
    private LocalTime reserveTime; //예약 시간
    private String department; // 진료과


}
