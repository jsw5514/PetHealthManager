package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
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
    public static String saveChat(ChatDTO chatDTO) {
        String chatPath = CHAT_STORAGE + "/" + chatDTO.getFileName();
        boolean isSaved = save(chatDTO.getContent(), chatPath);
        return isSaved ? chatPath : null;
    }

    //채팅 불러오기
    public static String loadChat(String chatFilePath) {
        //요구된 파일의 경로가 정상적인 데이터 파일 경로인지 확인
        if (!chatFilePath.startsWith(DATA_STORAGE)) {
            log.error("invalid chat file path: " + chatFilePath);
            return null;
        }
        String loadedContent = load(chatFilePath);
        log.info("chat file loaded in path: " + chatFilePath + " content: " + loadedContent);
        return loadedContent;
    }

    //데이터 저장
    public static String saveData(String content, String fileName) {
        String dataPath = DATA_STORAGE + "/" + fileName;
        boolean isSaved = save(content, dataPath);
        return isSaved ? dataPath : null;
    }
    
    //데이터 불러오기
    public static String loadData(String dataFilePath) {
        //요구된 파일의 경로가 정상적인 데이터 파일 경로인지 확인
        if (!dataFilePath.startsWith(DATA_STORAGE)) {
            log.error("invalid data file path: " + dataFilePath);
            return null;
        }
        String loadedContent = load(dataFilePath);
        log.info("data file loaded in path: " + dataFilePath + " content: " + loadedContent);
        return loadedContent;
    }
}
