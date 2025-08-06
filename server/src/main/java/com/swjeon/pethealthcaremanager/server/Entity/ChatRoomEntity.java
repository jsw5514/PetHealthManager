package com.swjeon.pethealthcaremanager.server.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


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
