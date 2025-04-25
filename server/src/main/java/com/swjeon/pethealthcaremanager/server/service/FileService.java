package com.swjeon.pethealthcaremanager.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class.getSimpleName());
    //채팅 저장 경로
    private static final String CHAT_STORAGE=System.getProperty("user.dir")+"/storage/chat";
    
    /** 파일 저장 함수
     * @param content 저장할 파일 내용
     * @param savePath 저장경로
     * @return 파일 저장 성공 여부
     */
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

    /** 파일 불러오기 함수
     * @param loadPath 불러올 파일 경로
     * @return 파일 내용
     */
    private static String load(String loadPath) {
        File file = new File(loadPath);

        if(!file.exists()){
            log.error("file not found. loadPath: " + loadPath);
            return null;
        }
        else {
            log.debug("file found. loadPath: " + loadPath);
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

    //채팅 저장
    public static String saveChat(String content, String fileName) {
        String chatPath = CHAT_STORAGE + "/" + fileName;
        boolean isSaved = save(content, chatPath);
        return isSaved ? chatPath : null;
    }

    //채팅 불러오기
    public static String loadChat(String chatFileName) {
        String chatPath = CHAT_STORAGE + "/" + chatFileName;
        String loadedContent = load(chatPath);
        log.debug(chatPath + " loaded content: " + loadedContent);
        return loadedContent;
    }
}
