package com.swjeon.pethealthcaremanager.server.Entity;

//auto-generated by groovy script

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor
public class ChatRoomEntity {

  @Column(name = "ID")
  private long id;

}
