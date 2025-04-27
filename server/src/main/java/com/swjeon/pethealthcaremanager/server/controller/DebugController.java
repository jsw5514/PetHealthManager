package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import com.swjeon.pethealthcaremanager.server.service.DataService;
import com.swjeon.pethealthcaremanager.server.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@RestController
public class DebugController {
    private Logger log = LoggerFactory.getLogger(DebugController.class);
    @Autowired
    private UsersService usersService;
    @Autowired
    private DataService dataService;

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

    //받은 요청 raw 데이터 출력 함수
    //@PostMapping("/login")
    public String readRawData(HttpServletRequest request) throws IOException {
        StringBuilder rawData = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                rawData.append(line).append("\n");
            }
        }
        System.out.println("Received Raw Data:\n" + rawData.toString()); // 콘솔 출력
        return "Raw Data received";
    }
}
