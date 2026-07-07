package com.petmilyday.dto.admin;

import com.petmilyday.entity.reservation.ReservationStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminReservationDTO {

    private Long id; //예약번호

    private String hospitalName;

    private String memberName;

    private LocalDate reservationDate;

    private LocalTime reservationTime; //예약 시간

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; //예약상태

}
