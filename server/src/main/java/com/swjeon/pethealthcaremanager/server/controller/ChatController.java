package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ChatController {
    /** 채팅 업로드 함수
     * @param roomId 채팅방 id
     * @param writerId 채팅 작성자 id
     * @param writeTime 채팅 작성 시간
     * @param contentType 채팅 내용 자료형
     * @param content 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     * @return 채팅 업로드 성공여부(boolean)
     */
    @PostMapping("/uploadChat")
    public String uploadChat(
            @RequestParam("roomId") int roomId,
            @RequestParam("writerId") String writerId,
            @RequestParam("writeTime") LocalDateTime writeTime,
            @RequestParam("contentType") String contentType,
            @RequestParam("content") String content)
    {
        return "not yet implemented"; //TODO not yet implemented
    }

    /** 채팅 내용 갱신 함수
     * @param roomId 채팅방 id
     * @param latestTimestamp 마지막으로 데이터를 갱신한 시점
     * @return 갱신된 채팅 내용(json 객체로 반환)
     *          roomId: 채팅방 Id
     *          contentList: 채팅 내용 배열(json 배열)
     *              writerNickname: 작성자 닉네임
     *              contentType: 채팅 내용 데이터 타입
     *              content: 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     */
    @PostMapping("/downloadChat")
    public String downloadChat(@RequestParam("roomId") int roomId, @RequestParam("latestTimestamp") LocalDateTime latestTimestamp) {
        return "not yet implemented"; //TODO not yet implemented
    }

    /**
     * @param creatorId 채팅방 생성자 id
     * @return 생성된 채팅방 id(int,생성 실패시 0)
     */
    @PostMapping("/createChatRoom")
    public String createChatRoom(@RequestParam("creatorId") String creatorId){
        return "not yet implemented"; //TODO not yet implemented
    }

    /**
     * @param roomId 초대할 채팅방 id
     * @param memberId 초대할 사람의 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/inviteChatMember")
    public String inviteChatMember(@RequestParam("roomId") int roomId, @RequestParam("memberId") String memberId) {
        return "not yet implemented"; //TODO not yet implemented
    }

    @PostMapping("/leaveChatRoom")
    public String leaveChatRoom(@RequestParam("roomId") int roomId, @RequestParam("memberId") String memberId) {
        return "not yet implemented"; //TODO not yet implemented
    }
}
