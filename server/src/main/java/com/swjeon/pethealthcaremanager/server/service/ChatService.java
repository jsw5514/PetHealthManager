package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {
    private final String LOCAL_STORAGE="/";// storage/chat";
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean uploadChat(int roomId, String writerId, LocalDateTime writeTime, String contentType, String content) 
    {
        final String chatPath = LOCAL_STORAGE + writerId + "_" + roomId + "_" + writeTime;
        
        //TODO content 내용을 파일로 저장 구현
        
        //db에 저장
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setRoomId(roomId);
        chatEntity.setWriterId(writerId);
        chatEntity.setWriteTime(writeTime);
        chatEntity.setContentType(contentType);
        chatEntity.setContentPath(chatPath);
        chatRepository.save(chatEntity);
        return false;//TODO not yet implemented
    }
}
