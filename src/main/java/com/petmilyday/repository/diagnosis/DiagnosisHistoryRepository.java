package com.petmilyday.repository.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisHistoryRepository
        extends JpaRepository<DiagnosisHistory, Long> {

    List<DiagnosisHistory>
    findByMember_IdOrderByCreatedAtDesc(Long memberId);

    Page<DiagnosisHistory> findByMember_IdOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );
}