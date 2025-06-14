package com.swjeon.pethealthcaremanager.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.service.FileService;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

/*ChatDTO 구조
* 업로드 시에만 필요(NonNull,WRITE_ONLY 적용)
* Integer roomId = 채팅방 id
* String writerId = 채팅 작성자 id
* -----------------------------
* 다운로드 시에만 필요(어노테이션 미적용)
* String writerNickname = 채팅 작성자 닉네임
* -----------------------------
* 업로드/다운로드 시 모두 필요(NonNull 적용)
* LocalDateTime writeTime = 채팅 작성 시간
* String contentType = 채팅 내용 자료형
*  String content = 채팅내용
* */
@Getter
@NoArgsConstructor
public class ChatDTO {
    @NonNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer roomId;
    
    @NonNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String writerId;
    
    private String writerNickname;
    
    @NonNull 
    private LocalDateTime writeTime;
    @NonNull
    private String contentType;
    @NonNull
    private String content;

    public ChatDTO(ChatEntity chatEntity, String contentVal){
        roomId = chatEntity.getRoomId();
        writerId = chatEntity.getWriterId();
        writeTime = chatEntity.getWriteTime();
        contentType = chatEntity.getContentType();
        content = contentVal;
    }

    @Override
    public String toString() {
        return "room" + roomId + "/[" + writerId + "(" + writerNickname + ")]: " + writeTime + " " + contentType
                + " " + content;
    }
}
