package com.petmilyday.controller.reservation;

import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    //예약 폼 페이지
    @GetMapping("/register")
    public String reservationForm(@RequestParam Long hospitalId, Model model){
        log.info("예약 폼 요청 - hospitalId: {} " , hospitalId);
        model.addAttribute("hospitalId",hospitalId);
        model.addAttribute("dto", new ReservationRequestDTO());
        return "reservation/reservationForm";
    }

    //예약신청
   @PostMapping("/register")
   public String reservationRegister(ReservationRequestDTO dto){
       log.info("예약 신청 - hospitalId: {}, petId: {}, date: {}, time: {}",
               dto.getHospitalId(), dto.getPetId(), dto.getReserveDate(), dto.getReserveTime());
       reservationService.reservationRegister(dto);
       return "redirect:/reservation/list";
   }

   //내 예약 목록
    @GetMapping("/list")
    public String reservationList(Model model){
        log.info("예약 목록 조회 요청");
        List<ReservationResponseDTO> reservationList = reservationService.reservationList(1L);
        model.addAttribute("reservationList",reservationList);
        return "reservation/reservationList";
    }

    //예약 취소
    @PostMapping("/{reservationId}/cancel")
    public String reservationCancel(@PathVariable Long reservationId,
                                    @RequestParam String cancelReason){
        log.info("예약 취소 요청 - reservationId: {}, 사유: {}", reservationId, cancelReason);
        reservationService.reservationCancel(reservationId,cancelReason);
        return "redirect:/reservation/list";
    }



}
