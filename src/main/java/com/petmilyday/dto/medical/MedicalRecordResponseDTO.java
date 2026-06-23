package com.petmilyday.dto.medical;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponseDTO {

    private Long id;

    private Long reservationId;

    private Long petId;

    private String petName;

    private String hospitalName;

    @NotNull(message = "진료일을 입력해주세요.")
    @PastOrPresent(message = "미래 날짜로는 진료기록을 작성할 수 없습니다.")
    private LocalDate visitDate;

    private String diagnosis;       // 진단 내용

    private String treatment;       // 치료 내용

    private String prescription;    // 처방 내용

    private Boolean vaccinated;     // 예방접종 여부

    private String vaccineName;     // 백신명

    private LocalDate nextVaccinationDate;  // 다음 접종 예정일

    private String memo;            // 특이사항

    private String pdfUrl;          // PDF URL
}