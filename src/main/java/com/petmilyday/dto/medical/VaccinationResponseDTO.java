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

    private Long petId;
    private String petName;
    private String species;

    private String vaccineName;
    private LocalDate vaccinatedDate;
    private LocalDate nextDate;
}