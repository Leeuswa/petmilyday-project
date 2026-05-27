package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationRepository extends JpaRepository<Vaccination,Long> {
    List<Vaccination> findByPetIdOrderByVaccinatedDateDesc(Long petId);
}
