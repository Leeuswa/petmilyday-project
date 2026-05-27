package com.petmilyday.dto.medical;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationResponseDTO {

    private Long id;
    private String petName;        // 반려동물 이름
    private String vaccineName;    // 백신 이름
    private LocalDate vaccinatedDate; // 접종일
    private LocalDate nextDate;    // 다음 접종일
}