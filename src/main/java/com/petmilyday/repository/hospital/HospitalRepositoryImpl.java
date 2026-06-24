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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // 기준 좌표가 있으면 거리순 정렬 경로로 처리 (그 외에는 기존 동작 그대로 유지)
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            return searchHospitalsPageByDistance(dto, pageable);
        }

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

    // 기준 좌표(검색한 지역 또는 내 주소)와 가까운 순으로 정렬한 병원 목록 페이징
    // 병원 수가 많지 않아 DB 함수 의존 없이 애플리케이션에서 하버사인 거리로 정렬한다.
    private Page<Hospital> searchHospitalsPageByDistance(HospitalRequestDTO dto, Pageable pageable) {
        QHospital hospital = QHospital.hospital;

        List<Hospital> filtered = queryFactory
                .selectFrom(hospital)
                .where(
                        keywordContains(dto.getKeyword()),
                        emergencyEq(dto.getIsEmergency()),
                        departmentContains(dto.getDepartment())
                )
                .fetch();

        List<Hospital> sorted = filtered.stream()
                .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
                .sorted(Comparator.comparingDouble(h -> distanceKm(
                        dto.getLatitude(), dto.getLongitude(),
                        h.getLatitude().doubleValue(), h.getLongitude().doubleValue()
                )))
                .collect(Collectors.toList());

        int total = sorted.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);

        return new PageImpl<>(sorted.subList(start, end), pageable, total);
    }

    // 하버사인 공식으로 두 좌표 간 거리(km) 계산
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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