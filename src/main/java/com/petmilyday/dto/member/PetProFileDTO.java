package com.petmilyday.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetProFileDTO {

    private Long id;

    @NotBlank(message = "반려동물의 이름을 입력해 주세요.")
    private String name;

    @NotBlank(message = "반려동물의 품종을 입력해 주세요.")
    private String species;

    @NotNull(message = "나이를 입력해 주세요.")
    @PositiveOrZero(message = "나이는 0 이상의 숫자여야 합니다.")
    private Integer age;

    @NotBlank(message = "반려동물의 품종(말티즈, 코숏 등)을 입력해 주세요.")
    private String breed;
}