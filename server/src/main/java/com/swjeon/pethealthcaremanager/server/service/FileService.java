package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class.getSimpleName());
    private static final String CHAT_STORAGE=System.getProperty("user.dir")+"/storage/chat";


    private static boolean save(String content, String savePath) {
        File file = new File(savePath);

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();

            log.debug("file saved. content: " + content + " save_path: " + savePath);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public static String saveChat(String content, String fileName) {
        String chatPath = CHAT_STORAGE + "/" + fileName;
        boolean isSaved = save(content, chatPath);
        return isSaved ? chatPath : null;
    }

    public static String loadChat(String chatFileName) {
        return "not yet implemented"; //TODO not yet implemented
    }
}
