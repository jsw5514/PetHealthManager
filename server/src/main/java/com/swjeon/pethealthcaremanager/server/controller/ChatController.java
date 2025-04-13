package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ChatController {
    /**
     * @param roomId 채팅방 id
     * @param writerId 채팅 작성자 id
     * @param writeTime 채팅 작성 시간
     * @param contentType 채팅 내용 자료형
     * @param content 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     * @return 채팅 업로드 성공여부(boolean)
     */
    @PostMapping
    public String uploadChat(
            @RequestParam("roomId")int roomId,
            @RequestParam("writerId") String writerId,
            @RequestParam("writeTime") LocalDateTime writeTime,
            @RequestParam("contentType") String contentType,
            @RequestParam("content") String content)
    {
        return "not yet implemented"; //TODO not yet implemented
    }

}
