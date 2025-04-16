package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatActionRepository {
    List<ChatEntity> getChatEntitiesByRoomIdAfter(int roomId, LocalDateTime latestTimestamp);
}
