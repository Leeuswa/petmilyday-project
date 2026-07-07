package com.petmilyday.dto.diagnosis;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResultDTO {

    private String disease;

    private String severity;

    private String recommend;
}