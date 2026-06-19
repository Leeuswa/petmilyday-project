package com.petmilyday.repository.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.QHospital;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Hospital> searchHospitals(HospitalRequestDTO dto) {
        QHospital hospital = QHospital.hospital;

        return queryFactory
                .selectFrom(hospital)
                .where(
                        keywordContains(dto.getKeyword()),
                        emergencyEq(dto.getIsEmergency()),
                        departmentContains(dto.getDepartment())
                )
                .orderBy(hospital.id.desc())
                .fetch();
    }

    @Override
    public Page<Hospital> searchHospitalsPage(HospitalRequestDTO dto, Pageable pageable) {
        QHospital hospital = QHospital.hospital;

        List<Hospital> content = queryFactory
                .selectFrom(hospital)
                .where(
                        keywordContains(dto.getKeyword()),
                        emergencyEq(dto.getIsEmergency()),
                        departmentContains(dto.getDepartment())
                )
                .orderBy(hospital.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(hospital.count())
                .from(hospital)
                .where(
                        keywordContains(dto.getKeyword()),
                        emergencyEq(dto.getIsEmergency()),
                        departmentContains(dto.getDepartment())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QHospital.hospital.name.contains(keyword);
    }

    private BooleanExpression emergencyEq(Boolean isEmergency) {
        if (isEmergency == null || !isEmergency) {
            return null;
        }

        return QHospital.hospital.isEmergency.eq(true);
    }

    private BooleanExpression departmentContains(String department) {
        if (department == null || department.isBlank()) {
            return null;
        }

        return QHospital.hospital.department.contains(department);
    }
}