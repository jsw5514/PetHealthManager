package com.swjeon.pethealthcaremanager.server.Entity;

//auto-generated by groovy script

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
  @Column(name = "CONTENT")
  private String content;

}
