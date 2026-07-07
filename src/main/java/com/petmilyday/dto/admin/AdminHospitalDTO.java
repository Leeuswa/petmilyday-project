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

    // 주소를 기반으로 서버에서 자동으로 채워지므로 사용자 입력값에 대한 검증은 하지 않음
    private BigDecimal latitude;

    private BigDecimal longitude;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;
}