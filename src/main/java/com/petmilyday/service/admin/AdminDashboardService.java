package com.petmilyday.service.admin;

import com.petmilyday.dto.admin.AdminDashboardDTO;

public interface AdminDashboardService {

    // 관리자 대시보드 통계 조회
    AdminDashboardDTO getDashboard();
}