package com.petmilyday.controller.admin;

import com.petmilyday.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // 메인 관리자 대시보드
    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        model.addAttribute("dashboard", adminDashboardService.getDashboard());

        return "admin/dashboard";
    }
}