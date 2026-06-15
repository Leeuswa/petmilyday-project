package com.petmilyday.dto.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DiagnosisHistoryDTO {

    private Long id;

    private Long petId;
    private String petName;

    private String symptomText;
    private String imageUrl;

    private String resultDisease;
    private String resultSeverity;
    private String resultRecommend;

    private LocalDateTime createdAt;

    public DiagnosisHistoryDTO(DiagnosisHistory history) {

        this.id = history.getId();

        this.petId = history.getPet().getId();
        this.petName = history.getPet().getName();

        this.symptomText = history.getSymptomText();
        this.imageUrl = history.getImageUrl();

        this.resultDisease = history.getResultDisease();
        this.resultSeverity = history.getResultSeverity();
        this.resultRecommend = history.getResultRecommend();

        this.createdAt = history.getCreatedAt();
    }
}