package com.swjeon.pethealthcaremanager.server.Repository.Impl;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatActionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;

public class ChatActionRepositoryImpl implements ChatActionRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ChatEntity> getChatEntitiesByRoomIdAfter(int roomId, LocalDateTime latestTimestamp) {
        final String sql = "SELECT * FROM CHAT WHERE ROOM_ID = ? AND WRITE_TIME > ?";
        Query query = entityManager.createNativeQuery(sql, ChatEntity.class);
        query.setParameter(1, roomId);
        query.setParameter(2, latestTimestamp == null ? "1970-01-01 00:00:01" : latestTimestamp);
        List<ChatEntity> result = query.getResultList();
        return result;
    }
}
