package com.petmilyday.controller.community;

import com.petmilyday.dto.community.ReportDTO;
import com.petmilyday.service.impl.community.ReportServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceImpl reportService;

    // 유저 비동기 신고
    @PostMapping("/api/reports")
    @ResponseBody
    public ResponseEntity<String> report(@RequestBody ReportDTO.Request requestDTO, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        reportService.registerReport(auth.getName(), requestDTO);
        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }

    // 관리자용 신고 내역 목록
    @GetMapping("/admin/community/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminReportList(Model model) {
        List<ReportDTO.Response> reports = reportService.getReportList();
        model.addAttribute("reports", reports);
        return "admin/report/communityReportList";
    }

    // 관리자용: 신고글 삭제 조치 승인
    @PostMapping("/admin/community/reports/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String reportActionDelete(@PathVariable("id") Long id) {
        reportService.processDelete(id);
        return "redirect:/admin/community/reports";
    }

    // 관리자용: 신고글 정상 유지 승인
    @PostMapping("/admin/community/reports/{id}/maintain")
    @PreAuthorize("hasRole('ADMIN')")
    public String reportActionMaintain(@PathVariable("id") Long id) {
        reportService.processMaintain(id);
        return "redirect:/admin/community/reports";
    }
}