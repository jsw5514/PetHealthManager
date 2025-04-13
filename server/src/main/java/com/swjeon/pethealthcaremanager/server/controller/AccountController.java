package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {
    //id 중복확인
    @RequestMapping(value = "/checkDuplicateId", method = {RequestMethod.GET, RequestMethod.POST})
    public String checkDuplicateId(@RequestParam("id") String id) {
        return "not yet implemented";//TODO not yet implemented
    }

    //회원가입
    @PostMapping("/signIn")
    public String signIn(@RequestParam("id") String id, @RequestParam("password") String password) {
        return "not yet implemented";//TODO not yet implemented
    }

    //로그인
    @PostMapping("/login")
    public String login(@RequestParam("id") String id, @RequestParam("password") String password) {
        return "not yet implemented";//TODO not yet implemented
    }
}
