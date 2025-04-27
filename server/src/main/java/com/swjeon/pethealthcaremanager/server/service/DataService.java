package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import com.swjeon.pethealthcaremanager.server.Repository.DataRepository;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DataService {
    private Logger log = LoggerFactory.getLogger(DataService.class);
    private DataRepository dataRepository;

    DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public boolean uploadData(DataDTO dataDTO) {
        //데이터 본문을 파일로 저장
        final String FILE_NAME = dataDTO.generateFileName();
        final String DATA_PATH = FileService.saveData(dataDTO.getData(), FILE_NAME);
        if (DATA_PATH == null){
            log.error("데이터 저장 실패");
            return false;
        }

        //본문을 제외한 나머지는 db에 저장
        DataEntity dataEntity = dataDTO.toEntity(DATA_PATH);
        try{
            dataRepository.save(dataEntity);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    public DataDTO downloadData(String downloaderId, String dataId) {
        //db에서 파일 경로를 포함한 정보 불러오기
        DataIdClass id = new DataIdClass(downloaderId,dataId);
        Optional<DataEntity> optionalDataEntity = dataRepository.findById(id);
        if(optionalDataEntity.isPresent()){ //db 검색에 성공한 경우
            DataEntity dataEntity = optionalDataEntity.get(); //db에 저장된 내용 불러오기

            //파일에서 데이터 내용 불러오기
            String dataContent = FileService.loadData(dataEntity.getDataPath());
            if(dataContent == null){
                log.error("failed to load data file");
                return null;
            }

            return DataDTO.fromEntity(dataEntity, dataContent); //DTO로 변환하여 반환
        }
        else{ //db 검색에 실패한 겨우
            log.error("no such data with downloaderId " + downloaderId + " dataId " + dataId);
            return null;
        }
    }
}
