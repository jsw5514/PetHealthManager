package com.swjeon.pethealthcaremanager.server.Entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "chat")
@Getter
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
  @Column(name = "CONTENT_PATH")
  private String contentPath;

}
