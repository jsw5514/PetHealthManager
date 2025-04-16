package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.ChatEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    private static String load(String loadPath) {
        File file = new File(loadPath);

        if(!file.exists()){
            log.error("file not found. loadPath: " + loadPath);
            return null;
        }
        else {
            log.info("file found. loadPath: " + loadPath);
            try{
                Path path = Path.of(loadPath);
                List<String> lines = Files.readAllLines(path);
                return String.join(System.lineSeparator(), lines);
            }
            catch (Exception e){
                log.error(e.getMessage());
                return null;
            }
        }
    }

    public static String saveChat(String content, String fileName) {
        String chatPath = CHAT_STORAGE + "/" + fileName;
        boolean isSaved = save(content, chatPath);
        return isSaved ? chatPath : null;
    }

    public static String loadChat(String chatFileName) {
        String chatPath = CHAT_STORAGE + "/" + chatFileName;
        String loadedContent = load(chatPath);
        log.info(chatPath + " loaded content: " + loadedContent);
        return loadedContent;
    }

}
