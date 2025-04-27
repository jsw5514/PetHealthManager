package com.swjeon.pethealthcaremanager.server.controller;

import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import com.swjeon.pethealthcaremanager.server.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DataController {
    private Logger log = LoggerFactory.getLogger(DataController.class);
    private DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /** 데이터 업로드 함수
     * @param dataDTO 데이터 객체
     * ----dataDTO 구조----
     * String uploaderId = 업로더 id
     * String dataId = 데이터 식별자
     *                 (같은 유저가 올린 데이터 사이에서 특정 데이터를 구별하기 위함, 클라이언트 임의로 설정
     * String metaData = 데이터 종류 등 데이터에 대해 추가로 저장하고 싶은 정보
     * String data = 데이터 자체(바이너리 데이터는 base64로 인코딩하여 전송)
     * -------------------
     * @return 요청 성공여부(boolean)
     */
    @PostMapping("/uploadData")
    public boolean uploadData(@RequestBody DataDTO dataDTO)
    {
        log.info("Upload attempt with data: " + dataDTO);
        return dataService.uploadData(dataDTO);
    }

    /** 데이터 다운로드 함수
     * @param downloadRequest 다운로드 요청 객체
     *                        downloaderId 다운로드 하려는 사람의 id(업로더와 동일해야함)
     *                        dataId 데이터 식별자
     * @return 원하는 데이터(dataDTO, 오류 발생시 null)
     */
    @PostMapping("/downloadData")
    public DataDTO downloadData(@RequestBody Map<String,String> downloadRequest)
    {
        log.info("Download attempt with data: " + downloadRequest);
        final String DOWNLOADER_ID = downloadRequest.get("downloaderId");
        final String DATA_ID = downloadRequest.get("dataId");
        return dataService.downloadData(DOWNLOADER_ID, DATA_ID);
    }
}
