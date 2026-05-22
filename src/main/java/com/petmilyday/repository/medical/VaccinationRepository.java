package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaccinationRepository extends JpaRepository<Vaccination,Long> {
}
