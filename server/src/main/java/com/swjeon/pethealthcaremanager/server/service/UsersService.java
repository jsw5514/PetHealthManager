package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import com.swjeon.pethealthcaremanager.server.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    //id 중복여부 확인
    public boolean checkDuplicateId(String id){
        return usersRepository.existsById(id); //JPA 기본 제공 함수 사용
    }

    public boolean signIn(UserDTO userDTO) {
        String id = userDTO.getId();
        String password = userDTO.getPassword();
        UsersEntity user;

        //id 중복 검사
        Optional<UsersEntity> optionalUser = usersRepository.findById(id);
        if(optionalUser.isPresent())
            return false;
        else{
            user = new UsersEntity();//TODO 엔티티화 매서드 추가
            user.setId(id);
            user.setPw(password);
            try{
                usersRepository.save(user);
            }
            catch (Exception e){
                log.error(e.getMessage());
                return false;
            }
            return true;
        }
    }

    public boolean login(UserDTO userDTO) {
        String id = userDTO.getId();
        String password = userDTO.getPassword();
        Optional<UsersEntity> optionalUser = usersRepository.findById(id);

        //id 검사
        if(optionalUser.isPresent()){
            UsersEntity user = optionalUser.get();
            return user.getPw().equals(password); //비밀번호 검사 및 결과 리턴
        }
        else
            return false;
    }
}
