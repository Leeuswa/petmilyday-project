package com.petmilyday.dto.hospital;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalHoursDTO {

    private Integer dayOfWeek; // 0=월 1=화 2=수 3=목 4=금 5=토 6=일
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;

}
