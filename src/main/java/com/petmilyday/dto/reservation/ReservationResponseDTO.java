package com.petmilyday.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private String petName;
    private LocalDate reserveDate;
    private LocalTime reserveTime;
    private String department;
    private String status;
    private Integer waitNumber;
    private String cancelReason;
    private LocalDateTime createdAt;
}