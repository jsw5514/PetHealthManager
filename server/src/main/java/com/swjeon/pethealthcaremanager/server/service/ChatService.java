package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import com.swjeon.pethealthcaremanager.server.Repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final String CHAT_STORAGE=System.getProperty("user.dir")+"/storage/chat";
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean uploadChat(int roomId, String writerId, LocalDateTime writeTime, String contentType, String content) 
    {
        String timeString = writeTime.toString().replace(":","-");
        final String chatPath = CHAT_STORAGE + "/" + writerId + "_" + roomId + "_" + timeString + ".txt";
        log.info("content: " + content + " save_path: " + chatPath);
        File file = new File(chatPath);

        
        //파일로 저장
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        
        //db에 저장
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setRoomId(roomId);
        chatEntity.setWriterId(writerId);
        chatEntity.setWriteTime(writeTime);
        chatEntity.setContentType(contentType);
        chatEntity.setContentPath(chatPath);
        chatRepository.save(chatEntity);
        return true;//TODO not yet implemented
    }
}
