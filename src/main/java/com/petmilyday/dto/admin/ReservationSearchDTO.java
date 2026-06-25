package com.petmilyday.dto.admin;

import com.petmilyday.entity.reservation.ReservationStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSearchDTO {

    private String hospitalName;
    private ReservationStatus status;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
