package com.petmilyday.controller.admin;

import com.petmilyday.service.admin.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    // 메인 관리자 전체 예약 현황 조회
    @GetMapping({ "/"})
    public String reservationList(Model model) {

        model.addAttribute("reservationList",
                adminReservationService.reservationList());

        return "admin/reservation/reservationList";
    }
}