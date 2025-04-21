package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.dto.UserDTO;
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
    public boolean signIn(@RequestBody UserDTO signInUser) {
        log.info("signIn id " + signInUser.getId() + " password " + signInUser.getPassword());
        return usersService.signIn(signInUser);
    }

    //로그인
    @PostMapping("/login")
    public boolean login(@RequestBody UserDTO loginUser) {
        log.info("Login attempt with id " + loginUser.getId() + " and password " + loginUser.getPassword());
        return usersService.login(loginUser);
    }
}
