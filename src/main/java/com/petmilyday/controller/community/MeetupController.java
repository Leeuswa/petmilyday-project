package com.petmilyday.controller.community;

import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.service.community.MeetupService;
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

    // 모임 목록 조회
    @GetMapping("/list")
    public String list(Model model) {
        List<MeetupPostDTO> list = meetupService.getList();

        // 화면의 responseDTO.dtoList 구조 맞춤 처리
        model.addAttribute("responseDTO", Map.of("dtoList", list));
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
    public String read(@RequestParam("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        MeetupPostDTO postDTO = meetupService.read(id, username);
        model.addAttribute("postDTO", postDTO);
        return "community/meetup_read";
    }

    // 모임 참여 처리
    @PostMapping("/join/{id}")
    public String join(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            meetupService.joinMeetup(id, username);
            redirectAttributes.addFlashAttribute("message", "모임 참여가 완료되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/community/meetup/read?id=" + id;
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
}