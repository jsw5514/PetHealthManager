package com.swjeon.pethealthcaremanager.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//외부에 노출되지 않는 유틸용 서비스
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class.getSimpleName());
    //채팅 저장 경로
    private static final String CHAT_STORAGE=System.getProperty("user.dir")+"/storage/chat";
    //일반 데이터 저장 경로
    private static final String DATA_STORAGE=System.getProperty("user.dir")+"/storage/data";
    
    /** 파일 저장 함수
     * @param content 저장할 파일 내용
     * @param savePath 저장경로
     * @return 파일 저장 성공 여부
     */
    private static boolean save(String content, String savePath) {
        File file = new File(savePath);

        try{
            log.info("Attempt to file save savePath: " + savePath + " content: " + content);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
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

    //데이터 저장
    public static String saveData(String content, String fileName) {
        String dataPath = DATA_STORAGE + "/" + fileName;
        boolean isSaved = save(content, dataPath);
        return isSaved ? dataPath : null;
    }
    
    //데이터 불러오기
    public static String loadData(String loadFileName) {
        String dataPath = DATA_STORAGE + "/" + loadFileName;
        String loadedContent = load(dataPath);
        log.info(dataPath + " loaded content: " + loadedContent);
        return loadedContent;
    }
}
