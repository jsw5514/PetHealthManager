package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Entity.ChatMemberEntity;
import com.swjeon.pethealthcaremanager.server.Entity.ChatRoomEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.ChatMemberIdClass;
import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatMemberRepository;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRoomRepository;
import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.util.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ChatMemberRepository chatMemberRepository, UsersRepository usersRepository) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.usersRepository = usersRepository;
    }

    /** 채팅 업로드 함수
     * @param chatDTO 채팅 객체
     */
    public void uploadChat(ChatDTO chatDTO) throws IOException
    {
        //전송자 닉네임 확인
        Optional<UsersEntity> writer = usersRepository.findById(chatDTO.getWriterId());
        if (writer.isEmpty()) {
            log.error("채팅 전송자를 찾을 수 없습니다. 존재하지 않는 유저가 전송한 채팅입니다.");
            throw new IllegalArgumentException();
        }
        chatDTO.setWriterNickname(writer.get().getNickname());
        
        if (chatDTO.getContentType().equals("text")) { //텍스트 채팅인 경우
            ChatEntity chatEntity = chatDTO.toEntity();
            chatRepository.save(chatEntity);
            return;
        }
        else{ //텍스트 채팅이 아닌 경우(base64로 인코딩 된 바이너리 데이터인 경우)
            //채팅 내용 파일로 저장
            String chatPath = FileManager.saveChat(chatDTO);
            if (chatPath == null){
                log.error("채팅 내용 파일 저장 실패");
                throw new IOException();
            }

            //파일 경로 및 나머지 데이터 db에 저장
            ChatEntity chatEntity = chatDTO.toEntity(chatPath);
            try{
                chatRepository.save(chatEntity);
            }
            catch (Exception e){
                log.error("파일 저장은 성공했으나 db 저장 중 에러가 발생함. "+chatPath+"의 파일은 삭제됨. "+e.getMessage());
                FileManager.deleteChat(chatPath);
                throw e;
            }
            return;
        }
    }


    /** 채팅 다운로드 함수
     * @param roomId 채팅을 가져올 채팅방 id
     * @param latestTimestamp 클라이언트가 갖고있는 가장 최신의 채팅 타임스탬프 
     * @return 업데이트된 채팅 내용들
     */
    public ArrayList<ChatDTO> downloadChat(int roomId, LocalDateTime latestTimestamp) {
        //db에서 파일 경로 및 기타 정보 불러오기
        List<ChatEntity> chatList = chatRepository.getChatEntitiesByRoomIdAfter(roomId,latestTimestamp);
        ArrayList<ChatDTO> chatDTOArrayList = new ArrayList<>();

        //채팅 파일 불러오기
        for(ChatEntity chatEntity : chatList){
            chatDTOArrayList.add(chatEntity.toDTO());
        }
        return chatDTOArrayList;
    }

    /** 채팅방 생성 함수
     * @param creatorId 채팅방 생성자 id
     */
    public int createChatRoom(String creatorId) {
        ChatRoomEntity inserted = chatRoomRepository.save(new ChatRoomEntity());
        int roomId = inserted.getId();
        chatMemberRepository.save(new ChatMemberEntity(roomId, creatorId));
        return roomId;
    }

    public void inviteChatMember(int roomId, String memberId) {
        chatMemberRepository.save(new ChatMemberEntity(roomId, memberId));
    }

    public void leaveChatRoom(int roomId, String memberId) {
        boolean isRoomPresent = chatRoomRepository.findById(roomId).isPresent();
        Optional<ChatMemberEntity> member = chatMemberRepository.findById(new ChatMemberIdClass(roomId, memberId));
        boolean isMemberPresent = member.isPresent();
        if (isRoomPresent && isMemberPresent) {
            chatMemberRepository.delete(member.get());
            if (chatMemberRepository.findByRoomId(roomId).isEmpty()){//check chat room is empty
                chatRoomRepository.deleteById(roomId);
            }
        }
        else {
            throw new IllegalStateException();
        }
    }

    public List<String> getChatMember(int roomId) {
        List<ChatMemberEntity> memberEntities = chatMemberRepository.findByRoomId(roomId);
        ArrayList<String> members = new ArrayList<>();
        for (ChatMemberEntity memberEntity : memberEntities) {
            Optional<UsersEntity> member = usersRepository.findById(memberEntity.getMemberId());
            if (member.isPresent()) 
                members.add(member.get().getNickname());
            else
                throw new IllegalStateException("채팅맴버 확인 중 오류 발생. 존재하지 않는 유저가 채팅맴버로 등록되어있음.");
        }
        return members;
    }

    public ResponseEntity<List<Map<String, String>>> getJoinedRooms(String userId) {
        ArrayList<Map<String, String>> joinedRooms = new ArrayList<>();
        List<ChatMemberEntity> list = chatMemberRepository.getChatMemberEntitiesByMemberId(userId);
        for(ChatMemberEntity memberEntity : list){
            Map<String,String> map = new HashMap<>();
            map.put("roomId",Integer.toString(memberEntity.getRoomId()));
            map.put("creatorId","testid");
            joinedRooms.add(map);
        }
        return ResponseEntity.ok(joinedRooms);
    }
}
