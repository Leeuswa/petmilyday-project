package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.Vaccination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {

    List<Vaccination> findByPetIdOrderByVaccinatedDateDesc(Long petId);

    // 로그인한 회원의 모든 동물 예방접종 기록을 최신 접종일 순으로 조회
    @Query("""
            SELECT v
            FROM Vaccination v
            JOIN FETCH v.pet p
            JOIN FETCH p.member m
            WHERE m.username = :username
            ORDER BY v.vaccinatedDate DESC
            """)
    List<Vaccination> findMyVaccinations(
            @Param("username") String username
    );

    // 로그인한 회원의 모든 동물 예방접종 기록 조회 + 페이징
    @Query(
            value = """
                    SELECT v
                    FROM Vaccination v
                    JOIN FETCH v.pet p
                    JOIN FETCH p.member m
                    WHERE m.username = :username
                    ORDER BY v.vaccinatedDate DESC
                    """,
            countQuery = """
                    SELECT count(v)
                    FROM Vaccination v
                    JOIN v.pet p
                    JOIN p.member m
                    WHERE m.username = :username
                    """
    )
    Page<Vaccination> findMyVaccinationsPage(
            @Param("username") String username,
            Pageable pageable
    );
}