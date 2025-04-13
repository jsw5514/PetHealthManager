package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    @GetMapping("/")
    public String index() {
        return "서버 접속 성공. url을 수정하여 구체적인 요청을 명시";
    }

    //디버그용
    @GetMapping("/signIn")
    public String noSignIn(@RequestParam("id") String id, @RequestParam("password") String password) {
        return "회원가입은 Post 매서드로 처리 id: " + id + " password: " + password;
    }
    @GetMapping("/login")
    public String noLogin(@RequestParam("id") String id, @RequestParam("password") String password) {
        return "로그인은 Post 매서드로 처리 id: " + id + " password: " + password;
    }
}
