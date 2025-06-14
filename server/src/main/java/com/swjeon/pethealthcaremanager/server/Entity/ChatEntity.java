package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.NonNull;
import lombok.Setter;

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
  @Column(name = "CONTENT_PATH")
  private String contentPath;
}
