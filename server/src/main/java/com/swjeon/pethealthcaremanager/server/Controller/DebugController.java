package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.DataService;
import com.swjeon.pethealthcaremanager.server.Service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class DebugController {
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
    //@PostMapping("/uploadData")
    public String readRawData(@RequestBody Map<String,String> request) throws IOException {
//        StringBuilder rawData = new StringBuilder();
//        try (BufferedReader reader = request.getReader()) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                rawData.append(line).append("\n");
//            }
//        }
//        log.info("Received Raw Data:\n" + rawData.toString()); // 콘솔 출력
        log.info("Upload attempt with data: " + request);
        return "Raw Data received";
    }
}
