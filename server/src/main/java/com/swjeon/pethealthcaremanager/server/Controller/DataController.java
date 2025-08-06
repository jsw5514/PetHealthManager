package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.DataService;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import com.swjeon.pethealthcaremanager.server.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/data")
public class DataController {
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
     * String dataType = 데이터 종류 등 데이터에 대해 추가로 저장하고 싶은 정보
     * String data = 데이터 자체(바이너리 데이터는 base64로 인코딩하여 전송)
     * -------------------
     * @return 요청 성공여부(boolean)
     */
    @PostMapping
    public ResponseEntity<Void> uploadData(@RequestBody DataDTO dataDTO)
    {
        log.info("Upload attempt with data: " + dataDTO);
        try {
            dataService.uploadData(dataDTO);
        }
        catch (Exception e) {
            throw switch (ExceptionUtil.getErrorCode(e)){
                case ExceptionUtil.FOREIGN_KEY_EXCEPTION -> {
                    log.error("데이터 업로드 중 외래키 예외 발생. " +
                            "유저 테이블에 존재하지 않는 유저 id, 혹은 Pet 테이블에 존재하지 않는 data id(pet id)로 업로드 시도됨. " +
                            "DataDTO: " + dataDTO);
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 유저 혹은 반려동물의 데이터입니다.");
                }
                case ExceptionUtil.UNIQUE_KEY_EXCEPTION -> {
                    log.error("데이터 업로드 중 유니크키 예외 발생. " +
                            "같은 유저 id, 데이터 id로 업로드된 데이터가 이미 존재함." +
                            "DataDTO: " + dataDTO);
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "data id(pet id)가 중복되었습니다. 다른 data id를 사용해주세요.");
                }
                default -> {
                    log.error("데이터 업로드 중 알 수 없는 예외 발생. " +
                            "DataDTO: " + dataDTO +
                            "예외: " + e);
                    yield new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류입니다. 서버 로그를 확인해주세요.");
                }
            };
        }
        return ResponseEntity.ok().build();
    }

    /** 데이터 다운로드 함수
     * @param downloaderId 다운로드 하려는 사람의 id(업로더와 동일해야함)
     * @param dataId 데이터 식별자
     * @param dataType 데이터 타입
     * @return 원하는 데이터(dataDTO, 오류 발생시 null)
     */
    @GetMapping
    public ResponseEntity<DataDTO> downloadData(@RequestParam String downloaderId, @RequestParam String dataId, @RequestParam String dataType)
    {
        log.info("Download attempt with downloaderId: " + downloaderId + ", dataId: " + dataId + ", dataType: " + dataType);
        DataDTO data;
        try {
            data = dataService.downloadData(downloaderId, dataId, dataType);
        }
        catch (NoSuchElementException e) {
            log.error("데이터 탐색 실패. userId: " + downloaderId + ", dataId: " + dataId + ", dataType: " + dataType);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청받은 데이터를 찾을 수 없습니다.");
        }
        catch (Exception e) {
            log.error("데이터 다운로드 중 알 수 없는 예외 발생. downloaderId: " + downloaderId + ", dataId: " + dataId + ", dataType: " + dataType +
                    ", 예외: "+e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 서버 로그를 확인해주세요.");
        }
        return ResponseEntity.ok(data);
    }
}
