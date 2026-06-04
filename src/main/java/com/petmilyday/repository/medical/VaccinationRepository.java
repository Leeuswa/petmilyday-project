package com.petmilyday.repository.medical;

import com.petmilyday.entity.medical.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VaccinationRepository extends JpaRepository<Vaccination,Long> {
    List<Vaccination> findByPetIdOrderByVaccinatedDateDesc(Long petId);

    //로그인한 회원의 모든 동물 예방접종 기록을 최신 접종일 순으로 조회
    @Query("""
            SELECT v
            FROM Vaccination v
            JOIN v.pet p
            JOIN p.member m
            WHERE m.username = :username
            ORDER BY v.vaccinatedDate DESC
            """)
    List<Vaccination> findMyVaccinations(
            @Param("username") String username
    );

}

