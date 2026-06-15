package com.petmilyday.dto.hospital;

import com.petmilyday.dto.admin.AdminHospitalHourDTO;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalAdminHospitalDTO {

    private Long id;

    private String name;
    private String address;
    private String phone;

    @NotNull(message = "응급진료 여부를 선택해주세요.")
    private Boolean isEmergency;

    @NotBlank(message = "진료과목을 입력해주세요.")
    private String department;

    @NotNull(message = "예약 간격을 입력해주세요.")
    @Min(value = 30, message = "예약 간격은 최소 30분 이상이어야 합니다.")
    private Integer slotIntervalMin;

    @NotNull(message = "최대 예약 수를 입력해주세요.")
    @Max(value = 5, message = "최대 예약 수는 5 이하이어야 합니다.")
    @Min(value = 1, message = "최소 예약 수는 1 이상이어야 합니다.")
    private Integer maxPerSlot;

    @Builder.Default
    private List<AdminHospitalHourDTO> hours = new ArrayList<>();
}