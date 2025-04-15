package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    /** 데이터 업로드 함수
     * @param uploaderId 업로드하는 사람의 id
     * @param dataId 업로더가 구분할 수 있도록 하는 데이터 식별자
     * @param metaData 데이터 타입, 데이터 설명 등 데이터에 대한 데이터
     * @param data 데이터 내용(바이너리 데이터는 Base64 인코딩 후 전송)
     * @return 요청 성공여부(boolean)
     */
    @PostMapping("/uploadData")
    public String uploadData(
            @RequestParam("uploaderId") String uploaderId ,
            @RequestParam("dataId") String dataId ,
            @RequestParam("metaData") String metaData ,
            @RequestParam("data") String data)
    {
        return "not yet implemented"; //TODO not yet implemented
    }

    /** 데이터 다운로드 함수
     * @param downloaderId 다운로드 하려는 사람의 id
     * @return 원하는 데이터(오류 발생시 null)
     */
    @PostMapping("/downloadData")
    public String downloadData(
            @RequestParam("downloaderId") String downloaderId,
            @RequestParam("dataId") String dataId)
    {
        return "not yet implemented"; //TODO not yet implemented
    }
}
