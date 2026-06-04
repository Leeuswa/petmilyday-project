package com.petmilyday.controller.reservation;

import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.dto.reservation.ReservationSlotDto;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.service.hospital.HospitalService;
import com.petmilyday.service.member.PetProfileService;
import com.petmilyday.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final PetProfileService petProfileService;
    private final HospitalService hospitalService;

    //예약 폼 페이지
    @GetMapping("/register")
    public String reservationForm(@RequestParam Long hospitalId,
            Authentication authentication
            ,Model model){
        log.info("예약 폼 요청 - hospitalId: {} " , hospitalId);

        HospitalResponseDTO dto = hospitalService.hospitalReadOne(hospitalId);
        model.addAttribute("hospitalDTO",dto);

        model.addAttribute("hospitalId",hospitalId);
        model.addAttribute("dto", new ReservationRequestDTO());


        List<PetProFileDTO> petList = petProfileService.petList(authentication.getName());
        model.addAttribute("petList",petList);

        return "reservation/reservationForm";
    }

    //예약신청
   @PostMapping("/register")
   public String reservationRegister(ReservationRequestDTO dto,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes){
      try {
          reservationService.reservationRegister(dto,authentication.getName());
          return "redirect:/reservation/list";
      }catch (RuntimeException e){
          redirectAttributes
                  .addFlashAttribute(
                          "errorMessage",
                          e.getMessage()
                  );
      }

       return "redirect:/reservation/register?hospitalId=" + dto.getHospitalId();

   }

       //내 예약 목록
        @GetMapping("/list")
        public String reservationList(Authentication authentication,Model model){
            log.info("예약 목록 조회 요청");
            List<ReservationResponseDTO> reservationList = reservationService.reservationList(authentication.getName());
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

    @GetMapping("/slots")
    @ResponseBody
    public List<ReservationSlotDto> getAvailableSlots(
            @RequestParam Long hospitalId,
            @RequestParam LocalDate date
    ) {

        return reservationService.getAvailableSlots(
                hospitalId,
                date
        );
    }

    //내 동물 진료기록
    @GetMapping("/medical-records")
    public String myMedicalRecords(Authentication authentication,
                                   Model model){
        log.info("내 동물 진료기록 요청");

        List<ReservationResponseDTO> medicalRecordList =
                reservationService.myMedicalRecords(authentication.getName());

        model.addAttribute("medicalRecordList",medicalRecordList);
        return "reservation/medicalRecords";
    }


}
