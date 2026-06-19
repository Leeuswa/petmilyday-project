package com.petmilyday.dto.admin;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHospitalDTO {

    private Long id;

    @NotBlank(message = "병원명을 입력해주세요.")
    private String name;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotNull(message = "위도를 입력해주세요.")
    @DecimalMin(value = "33.0", message = "위도 값이 너무 작습니다.")
    @DecimalMax(value = "39.0", message = "위도 값이 너무 큽니다.")
    private BigDecimal latitude;

    @NotNull(message = "경도를 입력해주세요.")
    @DecimalMin(value = "124.0", message = "경도 값이 너무 작습니다.")
    @DecimalMax(value = "132.0", message = "경도 값이 너무 큽니다.")
    private BigDecimal longitude;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;
}