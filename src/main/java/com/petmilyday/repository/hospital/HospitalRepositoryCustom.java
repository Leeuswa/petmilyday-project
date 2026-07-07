package com.petmilyday.repository.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.entity.hospital.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HospitalRepositoryCustom {

    List<Hospital> searchHospitals(HospitalRequestDTO dto);

    Page<Hospital> searchHospitalsPage(HospitalRequestDTO dto, Pageable pageable);
}