package com.petmilyday.controller.member;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.dto.member.MemberDTO;
import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.service.community.CommunityService;
import com.petmilyday.service.community.MeetupService;
import com.petmilyday.service.member.MemberService;
import com.petmilyday.service.member.PetProfileService;
import com.petmilyday.service.product.S3UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PetProfileService petProfileService;
    private final S3UploadService s3UploadService;
    private final CommunityService communityService;
    private final MeetupService meetupService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private String getS3BaseUrl() {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/";
    }

    // 회원가입 요청
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new MemberDTO.RegisterRequest());
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") MemberDTO.RegisterRequest dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        // 유효성 검사
        if (bindingResult.hasErrors()) {
            return "member/register";
        }

        try {
            memberService.register(dto);
            redirectAttributes.addFlashAttribute("successMsg", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/member/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/register";
        }
    }

    // 로그인 요청
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute MemberDTO.LoginRequest loginRequest,
                        jakarta.servlet.http.HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            MemberDTO.LoginResponse loginResponse = memberService.login(loginRequest);

            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            response.addCookie(cookie);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/login";
        }
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam("username") String username) {
        return memberService.checkUsername(username);
    }

    // 로그아웃 요청
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        redirectAttributes.addFlashAttribute("successMsg", "안전하게 로그아웃 되었습니다.");
        return "redirect:/";
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logoutPost(jakarta.servlet.http.HttpServletResponse response,
                             RedirectAttributes redirectAttributes) {
        return logout(response, redirectAttributes);
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        MemberDTO.MyPageResponse myPageInfo = memberService.getMyPageInfo(username);
        model.addAttribute("member", myPageInfo);
        return "member/mypage";
    }

    // 회원정보 수정 화면
    @GetMapping("/modify-profile")
    public String modifyProfileForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        MemberDTO.MyPageResponse info = memberService.getMyPageInfo(username);

        MemberDTO.UpdateRequest updateRequest = MemberDTO.UpdateRequest.builder()
                .name(info.getName())
                .nickname(info.getNickname())
                .email(info.getEmail())
                .phoneNumber(info.getPhoneNumber())
                .address(info.getAddress())
                .detailAddress(info.getDetailAddress())
                .bio(info.getBio())
                .build();

        model.addAttribute("updateRequest", updateRequest);
        return "member/modify-profile";
    }

    // 회원정보 수정 처리
    @PostMapping("/modify-profile")
    public String modifyProfile(@Valid @ModelAttribute("updateRequest") MemberDTO.UpdateRequest dto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/modify-profile";
        }

        String username = authentication.getName();
        try {
            memberService.updateProfile(username, dto);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/modify-profile";
        }

        return "redirect:/member/mypage";
    }

    // 비밀번호 변경 요청
    @GetMapping("/modify-password")
    public String modifyPasswordForm(Model model) {
        model.addAttribute("passwordRequest", new MemberDTO.UpdatePasswordRequest());
        return "member/modify-password";
    }

    // 비밀번호 변경 처리
    @PostMapping("/modify-password")
    public String modifyPassword(@Valid @ModelAttribute("passwordRequest") MemberDTO.UpdatePasswordRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/modify-password";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            memberService.updatePassword(username, request);
            redirectAttributes.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해 주세요.");
            return "redirect:/member/logout";
        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/modify-password";
        }
    }

    @GetMapping("/pet-profile")
    public String petProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<PetProFileDTO> petList = petProfileService.petList(username);
        model.addAttribute("petList", petList);
        model.addAttribute("newPet", new PetProFileDTO());

        return "member/pet-profile";
    }

    // 회원 탈퇴
    @PostMapping("/withdraw")
    public String withdraw(Authentication authentication,
                           jakarta.servlet.http.HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        try {
            // 1. 서비스단의 회원탈퇴 로직 호출 (파라미터로 username만 넘기도록 서비스 메서드 활용)
            memberService.withdraw(username);

            // 2. 즉시 로그아웃 처리 (JWT가 저장된 Authorization 쿠키 삭제)
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // 쿠키 수명을 0으로 만들어 브라우저에서 삭제
            response.addCookie(cookie);

            redirectAttributes.addFlashAttribute("successMsg", "회원탈퇴가 정상적으로 완료되었습니다. 그동안 이용해 주셔서 감사합니다.");
            return "redirect:/"; // 메인 페이지로 리다이렉트

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "회원탈퇴 처리 중 오류가 발생했습니다.");
            return "redirect:/member/mypage";
        }
    }

    // 반려동물 등록 처리
    @PostMapping("/pet-profile/register")
    public String registerPet(@Valid @ModelAttribute("newPet") PetProFileDTO petDTO,
                              BindingResult bindingResult,
                              @RequestParam("file") MultipartFile file,
                              Principal principal,
                              Model model) {

        // 유효성 에러 & 파일 누락 시 화면 유지
        if (file == null || file.isEmpty() || bindingResult.hasErrors()) {

            String errorMessage = "입력 정보를 다시 확인해 주세요.";
            if (file == null || file.isEmpty()) {
                errorMessage = "반려동물 사진은 필수입니다.";
            } else if (bindingResult.hasErrors()) {
                errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            }

            model.addAttribute("errorMsg", errorMessage);

            List<PetProFileDTO> petList = petProfileService.petList(principal.getName());
            model.addAttribute("petList", petList);

            model.addAttribute("newPet", petDTO);

            return "member/pet-profile";
        }

        try {
            String uploadedFileName = s3UploadService.uploadFile(file);
            String fullPhotoUrl = uploadedFileName;
            petDTO.setPhotoUrl(fullPhotoUrl);

            petProfileService.registerPet(principal.getName(), petDTO);

            return "redirect:/member/pet-profile";

        } catch (Exception e) {
            // 예외 발생 시 화면 유지
            model.addAttribute("errorMsg", "이미지 업로드 중 오류가 발생했습니다.");

            List<PetProFileDTO> petList = petProfileService.petList(principal.getName());
            model.addAttribute("petList", petList);
            model.addAttribute("newPet", petDTO);

            return "member/pet-profile";
        }
    }

    // 반려동물 수정 화면 이동
    @GetMapping("/pet-profile/modify/{id}")
    public String editPetForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        PetProFileDTO pet = petProfileService.getPet(id, username);
        model.addAttribute("petProfileForm", pet);

        return "member/petModify";
    }

    // 반려동물 수정 처리
    @PostMapping("/pet-profile/modify/{id}")
    public String editPet(@PathVariable("id") Long id,
                          @Valid @ModelAttribute("petProfileForm") PetProFileDTO petDTO,
                          BindingResult bindingResult,
                          @RequestParam(value = "file", required = false) MultipartFile file,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/petModify";
        }

        String username = authentication.getName();

        try {
            // 새로운 파일 업로드된 경우 : S3에 새로 저장, URL을 갱신
            if (file != null && !file.isEmpty()) {
                String uploadedFileName = s3UploadService.uploadFile(file);
                String newFullPhotoUrl = uploadedFileName;
                petDTO.setPhotoUrl(newFullPhotoUrl);
            }

            petProfileService.updatePet(id, petDTO, username);
            redirectAttributes.addFlashAttribute("successMsg", "반려동물 정보가 수정되었습니다.");

        } catch (Exception e) {
            bindingResult.reject("globalError", "이미지 업로드 중 오류가 발생했습니다.");
            return "member/petModify";
        }

        return "redirect:/member/pet-profile";
    }

    // 반려동물 삭제 처리
    @PostMapping("/pet-profile/delete/{id}")
    public String deletePet(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            petProfileService.deletePet(id, username);
            redirectAttributes.addFlashAttribute("successMsg", "프로필이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/pet-profile";
    }

    // 내가 쓴 커뮤니티 게시글 리스트 조회 화면 이동
    @GetMapping("/mypage/myPosts")
    public String myPosts(Principal principal, Model model) {
        String username = principal.getName();

        List<CommunityPostDTO> myPostList = communityService.getMyPosts(username);
        model.addAttribute("postList", myPostList);

        return "member/myPosts";
    }

    // 나와 관련된 모임 리스트 조회
    @GetMapping("/mypage/myMeetups")
    public String myMeetups(Principal principal, Model model) {
        String username = principal.getName();

        // 내가 만든 모임 리스트 가져오기
        List<MeetupPostDTO> myMeetupList = meetupService.getMyMeetups(username);
        model.addAttribute("meetupList", myMeetupList);

        // 내가 참여한 모임 리스트 가져오기
        List<MeetupPostDTO> participatedList = meetupService.getParticipatedMeetups(username);
        model.addAttribute("participatedList", participatedList);

        return "member/myMeetups";
    }
}