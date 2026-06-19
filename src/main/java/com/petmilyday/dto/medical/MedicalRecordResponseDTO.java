package com.petmilyday.dto.medical;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponseDTO {

    private Long reservationId;

    private Long petId;

    private String petName;

    private String hospitalName;

    private LocalDate visitDate;

    private String diagnosis;       // 진단 내용

    private String treatment;       // 치료 내용

    private String prescription;    // 처방 내용

    private Boolean vaccinated;     // 예방접종 여부

    private String vaccineName;     // 백신명

    private String memo;            // 특이사항

    private String pdfUrl;          // PDF URL
}