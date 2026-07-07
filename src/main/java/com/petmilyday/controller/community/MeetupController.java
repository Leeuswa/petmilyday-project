package com.petmilyday.controller.community;

import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.entity.community.MeetupParticipant;
import com.petmilyday.repository.community.MeetupParticipantRepository;
import com.petmilyday.service.community.MeetupService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/community/meetup")
@Log4j2
@RequiredArgsConstructor
public class MeetupController {

    private final MeetupService meetupService;
    private final MeetupParticipantRepository meetupParticipantRepository;

    @GetMapping({"", "/"})
    public String meetupIndex() {
        return "redirect:/community/meetup/list";
    }

    // 모임 목록 조회
    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<MeetupPostDTO> responseDTO = meetupService.getList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        return "community/meetup_list";
    }

    // 모임 등록 페이지 요청
    @GetMapping("/register")
    public String registerGET(Model model) {
        model.addAttribute("meetupPostDTO", new MeetupPostDTO());
        return "community/meetup_register";
    }

    // 모임 등록 처리
    @PostMapping("/register")
    public String registerPOST(@Valid @ModelAttribute("meetupPostDTO") MeetupPostDTO meetupPostDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "community/meetup_register";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        meetupService.registerMeetup(username, meetupPostDTO);
        redirectAttributes.addFlashAttribute("result", "registered");

        return "redirect:/community/meetup/list";
    }

    // 모임 상세 조회
    @GetMapping("/read")
    public String read(@RequestParam("id") Long id,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 조회수 중복 방지용 쿠키 검증
        Cookie[] cookies = request.getCookies();
        Cookie viewCookie = null;

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("meetupView")) {
                    viewCookie = cookie;
                    break;
                }
            }
        }
        // 쿠키 정보에 따른 조회수 업데이트 수행
        if (viewCookie == null) {
            Cookie newCookie = new Cookie("meetupView", "[" + id + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
            meetupService.updateViewCount(id);
        } else {
            if (!viewCookie.getValue().contains("[" + id + "]")) {
                viewCookie.setValue(viewCookie.getValue() + "_[" + id + "]");
                viewCookie.setPath("/");
                viewCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(viewCookie);
                meetupService.updateViewCount(id);
            }
        }

        MeetupPostDTO postDTO = meetupService.read(id, username);
        model.addAttribute("postDTO", postDTO);
        return "community/meetup_read";
    }

    // 모임 수정 페이지
    @GetMapping("/modify")
    public String modifyGET(@RequestParam("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        MeetupPostDTO postDTO = meetupService.read(id, username);
        model.addAttribute("postDTO", postDTO);
        return "community/meetup_modify";
    }

    // 모임 수정 처리
    @PostMapping("/modify")
    public String modifyPOST(@Valid @ModelAttribute("postDTO") MeetupPostDTO meetupPostDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "community/meetup_modify";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            meetupService.modifyMeetup(username, meetupPostDTO);
            redirectAttributes.addFlashAttribute("result", "modified");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/meetup/modify?id=" + meetupPostDTO.getId();
        }

        return "redirect:/community/meetup/read?id=" + meetupPostDTO.getId();
    }

    // 참여 신청
    @PostMapping("/join/{id}")
    public String joinMeetup(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        try {
            // 변경된 서비스 로직 호출
            meetupService.registerParticipant(id, username);
            redirectAttributes.addFlashAttribute("successMsg", "모임 참여를 신청했습니다. 방장의 수락을 기다려주세요.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/community/meetup/read?id=" + id;
    }

    // 방장 전용 신청자 명단 관리 페이지 이동
    @GetMapping("/manage/{id}")
    public String manageMeetup(@PathVariable("id") Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        MeetupPostDTO postDTO = meetupService.read(id, username);

        if (postDTO.getHostUsername() == null || !postDTO.getHostUsername().equals(username)) {
            return "redirect:/community/meetup/list";
        }

        List<MeetupParticipant> applicantList = meetupService.getApplicants(id, username);

        model.addAttribute("meetupPost", postDTO);
        model.addAttribute("applicantList", applicantList);

        return "community/meetupManage";
    }

    // 신청자 수락 처리
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            com.petmilyday.entity.community.MeetupParticipant participant =
                    meetupParticipantRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));
            Long meetupPostId = participant.getMeetupPost().getId();

            meetupService.approveParticipant(id);

            redirectAttributes.addFlashAttribute("successMsg", "참여 신청을 수락했습니다.");

            return "redirect:/community/meetup/manage/" + meetupPostId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/community/meetup/list";
        }
    }

    // 신청자 거절 처리
    @PostMapping("/reject/{id}")
    public String reject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            com.petmilyday.entity.community.MeetupParticipant participant =
                    meetupParticipantRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));
            Long meetupPostId = participant.getMeetupPost().getId();

            meetupService.rejectParticipant(id);

            redirectAttributes.addFlashAttribute("successMsg", "참여 신청을 거절했습니다.");

            return "redirect:/community/meetup/manage/" + meetupPostId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/community/meetup/list";
        }
    }

    // 모임 참여 취소 처리
    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            meetupService.cancelMeetup(id, username);
            redirectAttributes.addFlashAttribute("message", "모임 참여를 취소했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/community/meetup/read?id=" + id;
    }

    // 모임 게시글 삭제 처리
    @PostMapping("/remove")
    public String remove(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            meetupService.deleteMeetup(id, username);
            redirectAttributes.addFlashAttribute("result", "removed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/meetup/read?id=" + id;
        }

        return "redirect:/community/meetup/list";
    }
}