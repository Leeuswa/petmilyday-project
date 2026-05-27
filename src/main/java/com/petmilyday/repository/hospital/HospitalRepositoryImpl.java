package com.petmilyday.repository.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.QHospital;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Hospital> searchHospitals(HospitalRequestDTO dto) {
        QHospital hospital = QHospital.hospital;

        return queryFactory
                .selectFrom(hospital)
                .where(
                        //null이 들어오면 조건 무시
                        dto.getKeyword() != null ? hospital.name.contains(dto.getKeyword()) : null,
                        dto.getIsEmergency() != null && dto.getIsEmergency() ? hospital.isEmergency.eq(true) : null,
                        dto.getDepartment() != null && !dto.getDepartment().isEmpty() ? hospital.department.contains(dto.getDepartment()) : null
                )
                .fetch();




    }
}
