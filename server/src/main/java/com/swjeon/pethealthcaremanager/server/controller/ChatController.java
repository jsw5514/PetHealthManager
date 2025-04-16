package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@RestController
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** 채팅 업로드 함수
     * @param roomId 채팅방 id
     * @param writerId 채팅 작성자 id
     * @param writeTime 채팅 작성 시간
     * @param contentType 채팅 내용 자료형
     * @param content 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     * @return 채팅 업로드 성공여부(boolean)
     */
    @PostMapping("/uploadChat")
    public boolean uploadChat(
            @RequestParam("roomId") int roomId,
            @RequestParam("writerId") String writerId,
            @RequestParam("writeTime") LocalDateTime writeTime,
            @RequestParam("contentType") String contentType,
            @RequestParam("content") String content)
    {
        return chatService.uploadChat(roomId, writerId, writeTime, contentType, content);
    }

    /** 채팅 내용 갱신 함수
     * @param roomId 채팅방 id
     * @param latestTimestamp 마지막으로 데이터를 갱신한 시점, 데이터를 갱신한 적이 없는 경우 생략
     * @return 갱신된 채팅 내용(json으로 반환, 실패시 null)
     *          contentList: 채팅 내용 배열(json 배열)
     *              writerNickname: 작성자 닉네임
     *              contentType: 채팅 내용 데이터 타입
     *              content: 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     */
    @PostMapping("/downloadChat")
    public ArrayList<ChatDTO> downloadChat(@RequestParam("roomId") int roomId, @RequestParam(value = "latestTimestamp", required = false) LocalDateTime latestTimestamp) {
        log.info("downloadChat roomId={}, latestTimestamp={}", roomId, latestTimestamp);
        return chatService.downloadChat(roomId,latestTimestamp);
    }

    /** 채팅방 생성 함수
     * @param creatorId 채팅방 생성자 id
     * @return 생성된 채팅방 id(int,생성 실패시 0)
     */
    @PostMapping("/createChatRoom")
    public String createChatRoom(@RequestParam("creatorId") String creatorId){
        return "not yet implemented"; //TODO not yet implemented
    }

    /** 채팅 맴버를 채팅방에 초대하는 함수
     * @param roomId 초대할 채팅방 id
     * @param memberId 초대할 사람의 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/inviteChatMember")
    public String inviteChatMember(@RequestParam("roomId") int roomId, @RequestParam("memberId") String memberId) {
        return "not yet implemented"; //TODO not yet implemented
    }

    /** 채팅방 나가기 함수
     * @param roomId 채팅방 id
     * @param memberId 나갈 맴버 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/leaveChatRoom")
    public String leaveChatRoom(@RequestParam("roomId") int roomId, @RequestParam("memberId") String memberId) {
        return "not yet implemented"; //TODO not yet implemented
    }

    /** 채팅방 맴버 가져오기 함수
     * @param roomId 채팅방 id
     * @return 채팅방 맴버 닉네임 리스트(ArrayList<String>)
     */
    @PostMapping("/getChatMember")
    public String getChatMember(@RequestParam("roomId") int roomId) {
        return "not yet implemented"; //TODO not yet implemented
    }
}
