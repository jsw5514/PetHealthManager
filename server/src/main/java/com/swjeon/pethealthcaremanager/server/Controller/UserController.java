package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.UsersService;
import com.swjeon.pethealthcaremanager.server.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final UsersService usersService;

    @Autowired
    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    //id 중복확인
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkDuplicateId(@RequestParam("id") String id) {
        log.info("check duplicate id by id " + id);
        boolean result = usersService.checkDuplicateId(id);
        return ResponseEntity.ok(result);
    }

    //회원가입
    @PostMapping("/sign-in")
    public ResponseEntity<Void> signIn(@RequestBody UserDTO signInUser) {
        log.info("Sign in attempt with " + signInUser);
        try {
            usersService.signIn(signInUser);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"이 id를 쓰는 유저가 이미 존재합니다.");
        }
        return ResponseEntity.ok().build();
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO loginUser) {
        log.info("Login attempt with " + loginUser);
        UserDTO loginedUser = usersService.login(loginUser);
        return ResponseEntity.ok(loginedUser);
    }
}
