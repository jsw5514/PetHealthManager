package com.swjeon.pethealthcaremanager.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class FileHelper {
    
    /** 텍스트 파일 저장 함수
     * @param content 저장할 파일 내용
     * @param savePath 저장경로
     * @return 파일 저장 성공 여부
     */
    public static boolean save(String content, String savePath) {
        log.info("Attempt to save file savePath: " + savePath + " content: " + content);
        File file = new File(savePath);

        try{
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

    /** 텍스트 파일 불러오기 함수
     * @param loadPath 불러올 파일 경로
     * @return 파일 내용
     */
    public static String load(String loadPath) {
        log.info("Attempt to load file loadPath: " + loadPath);
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

    /** 파일 삭제 함수
     * @param filePath 삭제할 파일 경로
     * @return 파일 내용
     */
    public static boolean delete(String filePath) {
        log.warn("Attempt to delete file filePath: " + filePath);
        File file = new File(filePath);
        if(!file.exists()){
            log.error("file not found. filePath: " + filePath);
            return false;
        }
        else {
            return file.delete();
        }
    }
}
