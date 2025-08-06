package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import com.swjeon.pethealthcaremanager.server.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UsersService {
    private final PetRepository petRepository;
    UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, PetRepository petRepository) {
        this.usersRepository = usersRepository;
        this.petRepository = petRepository;
    }

    //id 중복여부 확인
    public boolean checkDuplicateId(String id){
        return usersRepository.existsById(id); //JPA 기본 제공 함수 사용
    }

    public void signIn(UserDTO userDTO) {
        //id 중복 검사
        if(checkDuplicateId(userDTO.getId())){
            log.error("해당 id를 사용하는 유저가 이미 존재합니다.");
            throw new IllegalStateException();
        }
        else{
            UsersEntity usersEntity = userDTO.toEntity();
            usersRepository.save(usersEntity);
        }
    }

    public UserDTO login(UserDTO userDTO) {
        String id = userDTO.getId();
        String password = userDTO.getPassword();
        Optional<UsersEntity> optionalUser = usersRepository.findById(id);

        //id 검사
        if(optionalUser.isPresent()){
            UsersEntity usersEntity = optionalUser.get();
            if(usersEntity.getPw().equals(password)) {//비밀번호 검사
                return usersEntity.toDTO(petRepository);
            }
        }
        return null;
    }
}
