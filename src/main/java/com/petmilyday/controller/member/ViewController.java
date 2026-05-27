package com.petmilyday.controller.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // 루트 경로(/) 접속 시 웹사이트의 대문인 index.html을 보여줍니다.
    @GetMapping("/")
    public String index() {
        // src/main/resources/templates/home/index.html 렌더링
        return "home/index";
    }
}