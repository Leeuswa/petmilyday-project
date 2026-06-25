package com.petmilyday.controller.hospital;

import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.service.hospital.HospitalAdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospitalAdmin/reservations")
public class HospitalAdminReservationController {

    private final HospitalAdminReservationService hospitalAdminReservationService;

    // 병원 관리자 예약 목록 상태/날짜 필터
    @GetMapping
    public String reservationList(Authentication authentication,
                                  @RequestParam(required = false) ReservationStatus status,
                                  @RequestParam(required = false) LocalDate dateFrom,
                                  @RequestParam(required = false) LocalDate dateTo,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {

        String username = authentication.getName();

        model.addAttribute("reservationPage",
                hospitalAdminReservationService.reservationList(username, status, dateFrom, dateTo, page));

        model.addAttribute("currentPage", page);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "hospitalAdmin/reservation/reservationList";
    }

    // 예약 승인
    @PostMapping("/{id}/approve")
    public String approveReservation(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        hospitalAdminReservationService.approveReservation(id, authentication.getName());

        redirectAttributes.addFlashAttribute("message", "예약을 승인했습니다.");

        return "redirect:/hospitalAdmin/reservations";
    }

    // 예약 거절
    @PostMapping("/{id}/reject")
    public String rejectReservation(@PathVariable Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        hospitalAdminReservationService.rejectReservation(id, authentication.getName());

        redirectAttributes.addFlashAttribute("message", "예약을 거절했습니다.");

        return "redirect:/hospitalAdmin/reservations";
    }
}