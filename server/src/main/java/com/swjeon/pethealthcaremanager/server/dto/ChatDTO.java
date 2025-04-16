package com.swjeon.pethealthcaremanager.server.dto;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.service.FileService;
import jakarta.persistence.Column;

import java.time.LocalDateTime;


public class ChatDTO {
    private int chatId;
    private int roomId;
    private String writerId;
    private LocalDateTime writeTime;
    private String contentType;
    private String content;

    public ChatDTO(ChatEntity chatEntity, String contentVal){
        chatId = chatEntity.getChatId();
        roomId = chatEntity.getRoomId();
        writerId = chatEntity.getWriterId();
        writeTime = chatEntity.getWriteTime();
        contentType = chatEntity.getContentType();
        content = contentVal;
    }
}
