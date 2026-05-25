package com.petmilyday.dto.medical;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponseDTO {
    private Long petId;
    private String hospitalName;
    private LocalDate visitDate;
    private String diagnosis;     // 진단 내용
    private String prescription;  // 처방 내용
    private String pdfUrl;        // PDF URL

}
