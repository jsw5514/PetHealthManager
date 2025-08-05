package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import io.micrometer.observation.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class PetService {
    private final Logger log = LoggerFactory.getLogger(PetService.class);
    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public ResponseEntity<Void> removePetProfile(String petId) {
        try {
            petRepository.deleteById(petId);
        }
        catch (Throwable e){
            if (e instanceof EmptyResultDataAccessException) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 프로필을 찾을 수 없습니다.");
            }
            else if (e instanceof DataIntegrityViolationException) {
                Throwable root = e.getCause();
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                if (root instanceof SQLIntegrityConstraintViolationException ex) {
                    switch (ex.getErrorCode()) {
                        case 1451:
                            //외래키 위반(일반적으로는 발생하지 않음)
                            log.error("외래키 위반 발생. 삭제 대상 데이터를 다른 테이블이 참조하고 있습니다.");
                            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류 발생. 로그를 확인해주세요.");
                        default:
                            log.error("알 수 없는 오류입니다.");
                            throw new RuntimeException(ex);
                    }
                }
            }
        }
        return ResponseEntity.ok().build();
    }
}
