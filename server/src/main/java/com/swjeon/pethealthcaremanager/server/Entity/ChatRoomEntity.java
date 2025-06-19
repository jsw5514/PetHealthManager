package com.swjeon.pethealthcaremanager.server.Entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor
public class ChatRoomEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;
}
