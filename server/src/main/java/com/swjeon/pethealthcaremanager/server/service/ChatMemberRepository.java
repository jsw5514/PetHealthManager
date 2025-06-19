package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Integer> {
}
