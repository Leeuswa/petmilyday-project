package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long > {

    List<MedicalRecord> findByPetIdOrderByCreatedAtDesc(Long petId);

    Optional<MedicalRecord> findByReservationId(Long reservationId);
}
