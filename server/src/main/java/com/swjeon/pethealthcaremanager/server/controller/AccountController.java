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
    public boolean signIn(@RequestParam("id") String id, @RequestParam("password") String password) {
        return usersService.signIn(id, password);
    }

    //로그인
    @PostMapping("/login")
    public boolean login(@RequestParam("id") String id, @RequestParam("password") String password) {
        return usersService.login(id, password);
    }
}
