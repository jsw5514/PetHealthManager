package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServerController {

    @GetMapping("/")
    public String index() {
        return "서버 접속 성공. url을 수정하여 구체적인 요청을 명시할 것";
    }
}
