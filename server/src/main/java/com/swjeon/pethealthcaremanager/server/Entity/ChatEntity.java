package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;


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
  @Column(name = "WRITE_TIME")
  private LocalDateTime writeTime;
  @Column(name = "CONTENT_TYPE")
  private String contentType;
  @Column(name = "CONTENT")
  private String content;
  
  public ChatDTO toDTO(@Autowired UsersRepository usersRepository) {
    ChatDTO chatDTO = new ChatDTO();
    chatDTO.setRoomId(roomId);
    chatDTO.setWriterId(writerId);
    chatDTO.setWriteTime(writeTime);
    chatDTO.setContentType(contentType);
    chatDTO.setContent(content);

    UsersEntity writer = usersRepository.findById(writerId).orElse(null);
    if (writer != null) {
      chatDTO.setWriterNickname(writer.getNickname());
    }
    else {
      chatDTO.setWriterNickname("탈퇴한 사용자");
    }
    return chatDTO;
  }
}
