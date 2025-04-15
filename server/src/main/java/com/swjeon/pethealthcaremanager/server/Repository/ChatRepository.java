package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity,String> {
}
