package com.petmilyday.controller.hospital;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 동물병원 관련 컨트롤러(com.petmilyday.controller.hospital)에서만 동작 - 다른 기능에는 영향 없음
@Log4j2
@ControllerAdvice(basePackages = "com.petmilyday.controller.hospital")
public class HospitalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {

        log.warn("병원 기능 처리 중 오류 발생", e);

        // 서비스 코드에서 의도적으로 던진 RuntimeException(예: "이미 신청했습니다")만 그대로 노출.
        // 그 외(Hibernate/DB 등 시스템 예외)는 기술적 메시지를 가리고 일반 안내 문구로 대체.
        String message = e.getClass() == RuntimeException.class
                ? e.getMessage()
                : "요청을 처리하는 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.";

        redirectAttributes.addFlashAttribute("globalError", message);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/hospital/list");
    }
}
