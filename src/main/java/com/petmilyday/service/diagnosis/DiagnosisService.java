package com.petmilyday.service.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DiagnosisService {

    DiagnosisHistory diagnose(
            Long memberId,
            Long petId,
            String symptomText,
            MultipartFile image
    ) throws IOException;

    Page<DiagnosisHistory> getHistory(Long memberId, Pageable pageable);
}