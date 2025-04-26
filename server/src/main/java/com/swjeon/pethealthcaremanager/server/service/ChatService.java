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
     * @param roomId 채팅방 id
     * @param writerId 작성자 id
     * @param writeTime 작성시간
     * @param contentType 채팅 내용의 데이터 타입
     * @param content 채팅 내용
     * @return 업로드 성공 여부
     */
    public boolean uploadChat(int roomId, String writerId, LocalDateTime writeTime, String contentType, String content)
    {
        //채팅 내용 파일로 저장
        final String TIME_STRING = writeTime.toString().replace(":","-");
        final String FILE_NAME = writerId + "_" + roomId + "_" + TIME_STRING + ".txt";
        final String CHAT_PATH = FileService.saveChat(content, FILE_NAME);
        if (CHAT_PATH == null){
            log.error("채팅 내용 저장 실패");
            return false;
        }
            

        //파일 경로 및 나머지 데이터 db에 저장
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setRoomId(roomId);
        chatEntity.setWriterId(writerId);
        chatEntity.setWriteTime(writeTime);
        chatEntity.setContentType(contentType);
        chatEntity.setContentPath(CHAT_PATH);
        try{
            chatRepository.save(chatEntity);
        }
        catch (Exception e){
            log.error("파일 저장은 성공했으나 db에서 에러가 발생함. "+CHAT_PATH+"의 파일을 지울것."+e.getMessage());//TODO 파일 삭제 API 제작하여 대체
            return false;
        }
        return true;
    }

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
