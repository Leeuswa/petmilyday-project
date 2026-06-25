package com.petmilyday.controller.admin;

import com.petmilyday.dto.admin.ReservationSearchDTO;
import com.petmilyday.service.admin.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    // 메인 관리자 전체 예약 현황 검색/필터 조회
    @GetMapping({"", "/"})
    public String reservationList(ReservationSearchDTO searchDTO,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {

        model.addAttribute("reservationPage",
                adminReservationService.reservationList(searchDTO, page));

        model.addAttribute("currentPage", page);
        model.addAttribute("searchDTO", searchDTO);

        return "admin/reservation/reservationList";
    }
}