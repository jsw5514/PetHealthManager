package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import com.swjeon.pethealthcaremanager.server.util.FileManager;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatEntity {

  @Id
  @Column(name = "CHAT_ID")
  private int chatId;
  @Column(name = "ROOM_ID")
  private int roomId;
  @Column(name = "WRITER_ID")
  private String writerId;
  @Column(name = "WRITER_NICKNAME")
  private String writerNickname;
  @Column(name = "WRITE_TIME")
  private LocalDateTime writeTime;
  @Column(name = "CONTENT_TYPE")
  private String contentType;
  @Column(name = "CONTENT")
  private String content;
  
  public ChatDTO toDTO() {
    ChatDTO chatDTO = new ChatDTO();
    chatDTO.setRoomId(roomId);
    chatDTO.setWriterId(writerId);
    chatDTO.setWriteTime(writeTime);
    chatDTO.setContentType(contentType);
    chatDTO.setWriterNickname(writerNickname);
    chatDTO.setWriterNickname(Objects.requireNonNullElse(writerNickname, "탈퇴한 사용자"));

    if (contentType.equals("text")){
      chatDTO.setContent(content);      
    }
    else {
      String chatTimeString = writeTime.toString().replace(":", "-");
      String chatFileName = writerId + "_" + roomId + "_" + chatTimeString + ".txt";
      chatDTO.setContent(FileManager.loadChat(chatFileName));
    }
    return chatDTO;
  }
}
