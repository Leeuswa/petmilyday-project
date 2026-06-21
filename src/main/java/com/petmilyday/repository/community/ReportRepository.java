package com.petmilyday.repository.community;

import com.petmilyday.entity.community.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByStatusAscIdDesc();

}