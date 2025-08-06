package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.PetService;
import com.swjeon.pethealthcaremanager.server.dto.PetDTO;
import com.swjeon.pethealthcaremanager.server.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pet")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/profile")
    public ResponseEntity<Void> uploadPetProfile(@RequestBody PetDTO petDTO) {
        log.info("Upload attempt with pet: " + petDTO);
        try {
            petService.uploadPetProfile(petDTO);
        }
        catch (Exception e) {
            throw switch (ExceptionUtil.getErrorCode(e)) {
                case ExceptionUtil.FOREIGN_KEY_EXCEPTION -> {
                    log.error("반려동물 프로필 저장중 외래키 예외 발생. " +
                            "반려동물 주인의 user id가 user 테이블에 존재하지 않음");
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 유저에 대한 반려동물 추가입니다.");
                }
                case ExceptionUtil.UNIQUE_KEY_EXCEPTION -> {
                    log.error("반려동물 프로필 저장중 유니크키 예외 발생. " +
                            "pet id에 해당하는 데이터가 이미 존재함.");
                    yield new ResponseStatusException(HttpStatus.BAD_REQUEST, "pet id가 중복되었습니다. 다른 pet id를 사용하세요.");
                }
                default -> {
                    log.error("알 수 없는 예외입니다. 예외: " + e);
                    yield new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 서버 로그를 확인해주세요.");
                }
            };
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> removePetProfile(@RequestBody Map<String, String> request) {
        String petId = request.get("petId");
        try {
            petService.removePetProfile(petId);
        }
        catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 프로필을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok().build();
    }
}
