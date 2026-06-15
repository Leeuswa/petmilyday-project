package com.petmilyday.controller.hospital;

import com.petmilyday.service.hospital.HospitalAdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospitalAdmin/reservations")
public class HospitalAdminReservationController {

    private final HospitalAdminReservationService hospitalAdminReservationService;

    // 병원 관리자 예약 목록
    @GetMapping
    public String reservationList(Authentication authentication,
                                  Model model) {

        String username = authentication.getName();

        model.addAttribute("reservationList",
                hospitalAdminReservationService.reservationList(username));

        return "hospitalAdmin/reservation/reservationList";
    }

    // 예약 승인
    @PostMapping("/{id}/approve")
    public String approveReservation(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {

        hospitalAdminReservationService.approveReservation(id);

        redirectAttributes.addFlashAttribute("message", "예약을 승인했습니다.");

        return "redirect:/hospitalAdmin/reservations";
    }

    // 예약 거절
    @PostMapping("/{id}/reject")
    public String rejectReservation(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {

        hospitalAdminReservationService.rejectReservation(id);

        redirectAttributes.addFlashAttribute("message", "예약을 거절했습니다.");

        return "redirect:/hospitalAdmin/reservations";
    }
}