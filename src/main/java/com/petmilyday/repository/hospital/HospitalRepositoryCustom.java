package com.petmilyday.repository.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.entity.hospital.Hospital;

import java.util.List;

public interface HospitalRepositoryCustom {
    List<Hospital> searchHospitals(HospitalRequestDTO dto);
}