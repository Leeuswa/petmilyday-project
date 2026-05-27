package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> ,HospitalRepositoryCustom {


}
