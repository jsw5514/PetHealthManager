package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /** 채팅 업로드 함수
     * @param chatDTO 채팅 객체
     * @return 업로드 성공 여부
     */
    public boolean uploadChat(ChatDTO chatDTO)
    {
        if (chatDTO.getContentType().equals("text")) { //텍스트 채팅인 경우
            ChatEntity chatEntity = chatDTO.toEntity();
            chatRepository.save(chatEntity);
            return true;
        }
        else{ //텍스트 채팅이 아닌 경우(base64로 인코딩 된 바이너리 데이터인 경우)
            //채팅 내용 파일로 저장
            String chatPath = FileService.saveChat(chatDTO);
            if (chatPath == null){
                log.error("채팅 내용 저장 실패");
                return false;
            }

            //파일 경로 및 나머지 데이터 db에 저장
            ChatEntity chatEntity = chatDTO.toEntity(chatPath);
            try{
                chatRepository.save(chatEntity);
            }
            catch (Exception e){
                log.error("파일 저장은 성공했으나 db에서 에러가 발생함. "+chatPath+"의 파일은 삭제됨. "+e.getMessage());
                FileService.deleteChat(chatPath);
                return false;
            }
            return true;
        }
    }

    //TODO java doc 추가
    public ArrayList<ChatDTO> downloadChat(int roomId, LocalDateTime latestTimestamp) {
        //db에서 파일 경로 및 기타 정보 불러오기
        List<ChatEntity> chatList = chatRepository.getChatEntitiesByRoomIdAfter(roomId,latestTimestamp);
        ArrayList<ChatDTO> chatDTOArrayList = new ArrayList<>();

        //채팅 파일 불러오기
        String chatFileName = null;
        String chatTimeString = null;
        String chatContent;
        for(ChatEntity chatEntity : chatList){
            chatTimeString = chatEntity.getWriteTime().toString().replace(":","-");
            chatFileName = chatEntity.getWriterId() + "_" + chatEntity.getRoomId() + "_" + chatTimeString + ".txt";
            chatContent = FileService.loadChat(chatFileName);
            chatDTOArrayList.add(new ChatDTO(chatEntity, chatContent));
        }
        return chatDTOArrayList; //TODO roomId만 나오는 버그
    }
}
