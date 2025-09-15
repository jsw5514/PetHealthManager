package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.ChatService;
import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
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
     * @return 채팅 업로드 성공여부(http 응답코드)
     */
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadChat(@RequestBody ChatDTO chatDTO)
    {
        log.info("Uploading chat: " + chatDTO);
        try {
            chatService.uploadChat(chatDTO);
            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 유저가 보낸 채팅입니다.");
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "채팅 내용 저장에 실패했습니다. 서버 로그를 확인해주세요.");
        }
        catch (Exception e) {
            throw switch (ExceptionUtil.getErrorCode(e)){
                case ExceptionUtil.FOREIGN_KEY_EXCEPTION -> {
                    log.error("채팅 업로드 중 외래키 예외 발생. 존재하지 않는 채팅방에 업로드 시도됨.");
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방에 업로드 시도된 채팅입니다.");
                }
                default -> {
                    log.error("채팅 업로드 중 알 수 없는 오류 발생. chatDTO: " + chatDTO + "예외: " + e);
                    yield new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 서버 로그를 확인해주세요.");
                }
            };
        }
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
    @PostMapping("/download")
    public ResponseEntity<List<ChatDTO>> downloadChat(@RequestBody Map<String,Object> request) {
        log.info("Downloading chat: " + request);
        int roomId = (Integer) request.get("roomId");
        LocalDateTime latestTimestamp = LocalDateTime.parse( (String) request.get("latestTimestamp"));
        List<ChatDTO> updatedChats = chatService.downloadChat(roomId,latestTimestamp);
        return ResponseEntity.ok(updatedChats);
    }

    /** 채팅방 생성 함수
     * creatorId 채팅방 생성자 id
     * @return 생성된 채팅방 id(int,생성 실패시 0)
     */
    @PostMapping("/room")
    public ResponseEntity<Integer> createChatRoom(@RequestBody Map<String, String> request) {
        log.info("Creating new room: " + request);
        String creatorId = request.get("creatorId");
        int chatroomId = chatService.createChatRoom(creatorId);
        return ResponseEntity.ok(chatroomId);
    }

    /** 채팅 맴버를 채팅방에 초대하는 함수
     * roomId 초대할 채팅방 id
     * memberId 초대할 사람의 id
     * @return 성공여부(boolean)
     */
    @PostMapping("/room/{roomId}/member")
    public ResponseEntity<Void> inviteChatMember(@PathVariable Integer roomId, @RequestBody Map<String,Object> request) {
        log.info("inviteChatMember roomId " + roomId + " memberId " + request.get("memberId"));
        String memberId = (String)request.get("memberId");
        try {
            chatService.inviteChatMember(roomId, memberId);
        }
        catch (Exception e){
            throw switch (ExceptionUtil.getErrorCode(e)) {
                case ExceptionUtil.FOREIGN_KEY_EXCEPTION -> {
                    //외래키 위반(유저, 혹은 채팅방이 없음)
                    log.error("채팅방 초대 중 외래키 오류 발생. 채팅방이나 초대대상 유저가 존재하지 않음.");
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 요청입니다. 채팅방이나 초대대상이 존재하지 않습니다.");
                }
                case ExceptionUtil.UNIQUE_KEY_EXCEPTION -> {
                    //유니크키 위반(이미 존재하는 데이터)'
                    log.error("채팅방 초대 중 유니크키 오류 발생. 해당 채팅방에 이미 해당 유저가 존재함.");
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 초대된 상대입니다.");
                }
                default -> {
                    log.error("채팅방 초대 중 알 수 없는 오류 발생. 예외: " + e);
                    yield new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 서버 로그를 확인해주세요.");
                }
            };
        }
        return ResponseEntity.ok().build();
    }

    /** 채팅방 나가기 함수
     * roomId 채팅방 id
     * memberId 나갈 맴버 id
     * @return 성공여부(boolean)
     */
    @DeleteMapping("/room/{roomId}/member/{memberId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable Integer roomId, @PathVariable String memberId) {
        try {
            chatService.leaveChatRoom(roomId, memberId);
        }
        catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "잘못된 요청입니다. 해당 채팅방이 존재하지 않거나 채팅방 내에 해당 맴버가 존재하지 않습니다.");
        }
        return ResponseEntity.ok().build();
    }

    /** 채팅방 맴버 가져오기 함수
     * roomId 채팅방 id
     * @return 채팅방 맴버 닉네임 리스트(ArrayList<String>)
     */
    @GetMapping("/room/{roomId}/member")
    public ResponseEntity<List<String>> getChatMember(@PathVariable Integer roomId) {
        List<String> members = null;
        try {
            members = chatService.getChatMember(roomId);
        }
        catch (IllegalStateException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다. 로그를 확인해주세요.");
        }
        catch (Exception e) {
            log.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 서버 로그를 확인해주세요.");
        }
        return ResponseEntity.ok(members);
    }
    
    @PostMapping("/room/list")
    public ResponseEntity<List<Map<String,String>>> getRoomList(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        return chatService.getJoinedRooms(userId);
    }
}
