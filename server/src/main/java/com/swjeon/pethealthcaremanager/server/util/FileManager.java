package com.swjeon.pethealthcaremanager.server.util;

import com.swjeon.pethealthcaremanager.server.dto.ChatDTO;
import lombok.extern.slf4j.Slf4j;

import static com.swjeon.pethealthcaremanager.server.util.FileUtil.*;

@Slf4j
public class FileManager {
    //채팅 파일 저장 경로
    private static final String CHAT_STORAGE=System.getProperty("user.dir")+"/storage/chat";
    //일반 데이터 저장 경로
    private static final String DATA_STORAGE=System.getProperty("user.dir")+"/storage/data";

    //채팅 파일 저장
    public static String saveChat(ChatDTO chatDTO) {
        String chatPath = CHAT_STORAGE + "/" + chatDTO.getFileName();
        boolean isSaved = save(chatDTO.getContent(), chatPath);
        return isSaved ? chatPath : null;
    }

    //채팅 불러오기
    public static String loadChat(String chatFilePath) {
        //요구된 파일의 경로가 정상적인 채팅 파일 경로인지 확인
        if (!chatFilePath.startsWith(CHAT_STORAGE)) {
            log.error("invalid chat file path: " + chatFilePath);
            return null;
        }
        String loadedContent = load(chatFilePath);
        log.info("chat file loaded in path: " + chatFilePath + " content: " + loadedContent);
        return loadedContent;
    }

    //채팅 삭제(db 오류시 문제 파일 삭제를 위함)
    public static boolean deleteChat(String chatFilePath) {
        //삭제 요청된 파일의 경로가 정상적인 채팅 파일 경로인지 확인
        if(!chatFilePath.startsWith(CHAT_STORAGE)){
            log.error("invalid chat file path: " + chatFilePath);
            return false;
        }
        return delete(chatFilePath);
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
