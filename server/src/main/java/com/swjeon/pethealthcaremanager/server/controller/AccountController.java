package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AccountController {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final UsersService usersService;

    @Autowired
    public AccountController(UsersService usersService) {
        this.usersService = usersService;
    }

    //id 중복확인
    @GetMapping("/checkDuplicateId")
    public boolean checkDuplicateId(@RequestParam("id") String id) {
        log.info("check duplicate id by id " + id);
        return usersService.checkDuplicateId(id);
    }

    //회원가입
    @PostMapping("/signIn")
    public boolean signIn(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String password = request.get("password");
        log.info("signIn id " + id + " password " + password);
        return usersService.signIn(id, password);
    }

    //로그인
    @PostMapping("/login")
    public boolean login(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String password = request.get("password");
        log.info("Login attempt with id " + id + " and password " + password);
        return usersService.login(id, password);
    }
}
