package com.petmilyday.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHospitalHourDTO {

    private Long id;

    @NotNull(message = "요일을 선택해주세요.")
    @Min(value = 0, message = "요일 값은 0 이상이어야 합니다.")
    @Max(value = 6, message = "요일 값은 6 이하이어야 합니다.")
    private Integer dayOfWeek;

    private LocalTime openTime;

    private LocalTime closeTime;

    @NotNull(message = "휴무 여부를 선택해주세요.")
    private Boolean isClosed;
}