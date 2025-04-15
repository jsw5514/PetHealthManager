package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    /**
     * @param uploaderId 업로드하는 사람의 id
     * @param dataId 업로더가 구분할 수 있도록 하는 데이터 식별자
     * @param metaData 데이터 타입, 데이터 설명 등 데이터에 대한 데이터
     * @param data 데이터 내용
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

    /**
     * @param downloaderId
     * @return
     */
    @PostMapping("/downloadData")
    public String downloadData(
            @RequestParam("downloaderId") String downloaderId,
            @RequestParam("dataId") String dataId)
    {
        return "not yet implemented"; //TODO not yet implemented
    }
}
