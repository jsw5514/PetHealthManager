package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import com.swjeon.pethealthcaremanager.server.Repository.DataRepository;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class DataService {
    private DataRepository dataRepository;

    DataService(DataRepository dataRepository, PetRepository petRepository) {
        this.dataRepository = dataRepository;
    }

    public void uploadData(DataDTO dataDTO) {
        //db에 저장
        DataEntity dataEntity = dataDTO.toEntity();
        dataRepository.save(dataEntity);
    }

    public DataDTO downloadData(String downloaderId, String dataId, String dataType) {
        //db에서 파일 경로를 포함한 정보 불러오기
        DataIdClass id = new DataIdClass(downloaderId,dataId,dataType);
        Optional<DataEntity> optionalDataEntity = dataRepository.findById(id);
        if(optionalDataEntity.isPresent()){ //db 검색에 성공한 경우
            DataEntity dataEntity = optionalDataEntity.get(); //db에 저장된 내용 불러오기
            return dataEntity.toDTO(); //DTO로 변환하여 반환
        }
        else{ //db 검색에 실패한 경우
            throw new NoSuchElementException();
        }
    }
}
