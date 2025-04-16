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
        final String timeString = writeTime.toString().replace(":","-");
        final String fileName = writerId + "_" + roomId + "_" + timeString + ".txt";
        final String chatPath = FileService.saveChat(content, fileName);
        if (chatPath == null)
            return false;

        //파일 경로 및 나머지 데이터 db에 저장
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setRoomId(roomId);
        chatEntity.setWriterId(writerId);
        chatEntity.setWriteTime(writeTime);
        chatEntity.setContentType(contentType);
        chatEntity.setContentPath(chatPath);
        chatRepository.save(chatEntity);
        return true;
    }

    public ArrayList<ChatDTO> downloadChat(int roomId, LocalDateTime latestTimestamp) {
        //db에서 파일 경로 및 기타 정보 불러오기
        List<ChatEntity> chatList = chatRepository.getChatEntitiesByRoomIdAfter(roomId,latestTimestamp);
        ArrayList<ChatEntity> chatArrayList = new ArrayList<>(chatList);
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
        return chatDTOArrayList;
    }
}
