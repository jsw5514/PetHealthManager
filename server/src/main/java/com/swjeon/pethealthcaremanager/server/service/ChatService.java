package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Entity.ChatMemberEntity;
import com.swjeon.pethealthcaremanager.server.Entity.ChatRoomEntity;
import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRoomRepository;
import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
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
     * @return 업로드 성공 여부
     */
    public boolean uploadChat(ChatDTO chatDTO)
    {
        if (chatDTO.getContentType().equals("text")) { //텍스트 채팅인 경우
            Optional<UsersEntity> writer = usersRepository.findById(chatDTO.getWriterId());
            if (writer.isEmpty()) {
                log.error("채팅 전송자를 찾을 수 없습니다. 존재하지 않는 유저가 전송한 채팅입니다.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 유저가 보낸 채팅입니다.");
            }
            ChatEntity chatEntity = chatDTO.toEntityWithNickname(writer.get().getNickname());
            chatRepository.save(chatEntity);
            return true;
        }
        else{ //텍스트 채팅이 아닌 경우(base64로 인코딩 된 바이너리 데이터인 경우)
            //채팅 내용 파일로 저장
            String chatPath = FileUtil.saveChat(chatDTO);
            if (chatPath == null){
                log.error("채팅 내용 저장 실패");
                return false;
            }

            //파일 경로 및 나머지 데이터 db에 저장
            ChatEntity chatEntity = chatDTO.toEntityWithPath(chatPath);
            try{
                chatRepository.save(chatEntity);
            }
            catch (Exception e){
                log.error("파일 저장은 성공했으나 db에서 에러가 발생함. "+chatPath+"의 파일은 삭제됨. "+e.getMessage());
                FileUtil.deleteChat(chatPath);
                return false;
            }
            return true;
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
        String chatFileName = null;
        String chatTimeString = null;
        String chatContent;
        for(ChatEntity chatEntity : chatList){
            chatDTOArrayList.add(chatEntity.toDTO());
        }
        return chatDTOArrayList;
    }

    public int createChatRoom(String creatorId) {
        ChatRoomEntity inserted = chatRoomRepository.save(new ChatRoomEntity());
        int roomId = inserted.getId();
        chatMemberRepository.save(new ChatMemberEntity(roomId, creatorId));
        return roomId;
    }
}
