package com.petmilyday.repository.hospital;

import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.HospitalHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalHoursRepository extends JpaRepository<HospitalHours,Long> {

    // 병원 + 요일 운영시간 조회
    Optional<HospitalHours> findByHospitalAndDayOfWeek(Hospital hospital, Integer dayOfWeek );
}
