package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean uploadChat(int roomId, String writerId, LocalDateTime writeTime, String contentType, String content) 
    {
        return false;//TODO not yet implemented
    }
}
