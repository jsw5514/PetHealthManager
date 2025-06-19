package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
}
