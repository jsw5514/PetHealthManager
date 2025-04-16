package com.swjeon.pethealthcaremanager.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.service.FileService;
import jakarta.persistence.Column;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor

public class ChatDTO {
    @JsonProperty("chatId")
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
