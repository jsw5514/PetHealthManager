package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** 채팅 업로드 함수
     * @param chatDTO 채팅 객체
     * ----chatDTO 구조----
     * Integer roomId = 채팅방 id
     * String writerId = 채팅 작성자 id
     * String writerNickname = 채팅 작성자 닉네임
     * LocalDateTime writeTime = 채팅 작성 시간
     * String contentType = 채팅 내용 자료형
     * String content = 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     * -------------------
     * @return 채팅 업로드 성공여부(boolean)
     */
    @PostMapping("/uploadChat")
    public ResponseEntity<Void> uploadChat(@RequestBody ChatDTO chatDTO)
    {
        log.info("Uploading chat: {}", chatDTO);
        return chatService.uploadChat(chatDTO);
    }

    /** 채팅 내용 갱신 함수
     * @param request 요청 파라미터
     * ----request 구조----
     * roomId 채팅방 id
     * latestTimestamp 마지막으로 데이터를 갱신한 시점, 데이터를 갱신한 적이 없는 경우 생략
     * -------------------
     * @return 갱신된 채팅 내용(json으로 반환, 실패시 null)
     *          contentList: 채팅 내용 배열(json 배열)
     *          writeTime: 작성 시간
     *          writerNickname: 작성자 닉네임
     *          contentType: 채팅 내용 데이터 타입
     *          content: 채팅내용(바이너리 데이터는 Base64 인코딩 후 전송)
     */
    @PostMapping("/downloadChat")
    public ArrayList<ChatDTO> downloadChat(@RequestBody Map<String,Object> request) {
        int roomId = (Integer) request.get("roomId");
        LocalDateTime latestTimestamp = LocalDateTime.parse( (String) request.get("latestTimestamp"));
        log.info("downloadChat room {}, after {}", roomId, latestTimestamp);
        return chatService.downloadChat(roomId,latestTimestamp);
    }

    /** 채팅방 생성 함수
     * creatorId 채팅방 생성자 id
     * @return 생성된 채팅방 id(int,생성 실패시 0)
     */
    @PostMapping("/createChatRoom")
    public int createChatRoom(@RequestBody Map<String, String> request) {
        String creatorId = request.get("creatorId");
        log.info("createChatRoom creatorId {}", creatorId);
        return chatService.createChatRoom(creatorId);
    }

    /** 채팅 맴버를 채팅방에 초대하는 함수
     * roomId 초대할 채팅방 id
     * memberId 초대할 사람의 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/inviteChatMember")
    public ResponseEntity<Void> inviteChatMember(@RequestBody Map<String,Object> request) {
        int roomId = (Integer)request.get("roomId");
        String memberId = (String)request.get("memberId");
        return chatService.inviteChatMember(roomId, memberId);
    }

    /** 채팅방 나가기 함수
     * roomId 채팅방 id
     * memberId 나갈 맴버 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/leaveChatRoom")
    public ResponseEntity<Void> leaveChatRoom(@RequestBody Map<String,Object> request) {
        int roomId = (Integer)request.get("roomId");
        String memberId = (String)request.get("memberId");
        return chatService.leaveChatRoom(roomId, memberId);
    }

    /** 채팅방 맴버 가져오기 함수
     * roomId 채팅방 id
     * @return 채팅방 맴버 닉네임 리스트(ArrayList<String>)
     */
    @PostMapping("/getChatMember")
    public ResponseEntity<Void> getChatMember(@RequestBody Map<String,Object> request) {
        int roomId = (Integer)request.get("roomId");
        return chatService.getChatMember(roomId);
    }
}
