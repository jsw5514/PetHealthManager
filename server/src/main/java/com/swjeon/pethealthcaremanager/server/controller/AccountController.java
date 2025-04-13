package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {
    private UsersService usersService;

    @Autowired
    public AccountController(UsersService usersService) {
        this.usersService = usersService;
    }

    //id 중복확인
    @RequestMapping(value = "/checkDuplicateId", method = {RequestMethod.GET, RequestMethod.POST})
    public boolean checkDuplicateId(@RequestParam("id") String id) {
        return usersService.checkDuplicateId(id);
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
