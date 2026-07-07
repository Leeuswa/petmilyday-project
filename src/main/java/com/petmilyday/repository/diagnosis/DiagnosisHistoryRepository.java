package com.petmilyday.repository.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisHistoryRepository
        extends JpaRepository<DiagnosisHistory, Long> {

    @EntityGraph(attributePaths = {"pet"})
    Page<DiagnosisHistory> findByMember_IdOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );
}