package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.ChatMemberEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.ChatMemberIdClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, ChatMemberIdClass> {
    @Query(value = "SELECT * FROM CHAT_MEMBER " +
            "WHERE ROOM_ID = :roomId", nativeQuery = true)
    List<ChatMemberEntity> findByRoomId(@Param("roomId") int roomId);

    List<ChatMemberEntity> getChatMemberEntitiesByMemberId(String memberId);
}
