package com.petmilyday.controller.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.service.community.CommunityService;
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

@Controller
@RequestMapping("/community")
@Log4j2
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityService communityPostService;

    @GetMapping({"", "/"})
    public String communityIndex() {
        return "redirect:/community/list";
    }

    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<CommunityPostDTO> responseDTO = communityPostService.getList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);

        return "community/list";
    }

    @GetMapping("/register")
    public String registerGET(Model model) {
        model.addAttribute("postDTO", new CommunityPostDTO());
        return "community/register";
    }

    @PostMapping("/register")
    public String registerPOST(@Valid @ModelAttribute("postDTO") CommunityPostDTO postDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        // 유효성 검사(빈칸, 길이 등) 실패 시 작성 화면으로 되돌려보냄
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/community/register";
        }

        // 시큐리티에 보관된 현재 로그인 유저의 아이디 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Long id = communityPostService.registerPost(username, postDTO);

        redirectAttributes.addFlashAttribute("result", id);

        return "redirect:/community/list";
    }

    @GetMapping("/read")
    public String read(@RequestParam("id") Long id, Model model,
                       HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        boolean isViewed = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postView_" + id)) {
                    isViewed = true;
                    break;
                }
            }
        }

        if (!isViewed) {
            communityPostService.updateViewCount(id);
            Cookie viewCookie = new Cookie("postView_" + id, "viewed");
            viewCookie.setMaxAge(60 * 60 * 24);
            viewCookie.setPath("/");
            response.addCookie(viewCookie);
        }

        CommunityPostDTO postDTO = communityPostService.readPost(id);
        model.addAttribute("postDTO", postDTO);
        return "community/read";
    }

    @GetMapping("/modify")
    public String modifyGET(@RequestParam("id") Long id, Model model) {
        CommunityPostDTO postDTO = communityPostService.readPost(id);
        model.addAttribute("postDTO", postDTO);
        return "community/modify";
    }

    @PostMapping("/modify")
    public String modify(@Valid @ModelAttribute("dto") CommunityPostDTO postDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/community/modify?id=" + postDTO.getId();
        }

        communityPostService.modifyPost(postDTO);
        redirectAttributes.addFlashAttribute("result", "modified");

        return "redirect:/community/read?id=" + postDTO.getId();
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {

        communityPostService.removePost(id);
        redirectAttributes.addFlashAttribute("result", "removed");

        return "redirect:/community/list";
    }

}