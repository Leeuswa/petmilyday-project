package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long > {
}
