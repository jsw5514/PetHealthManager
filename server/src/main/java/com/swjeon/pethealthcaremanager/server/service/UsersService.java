package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import com.swjeon.pethealthcaremanager.server.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService {
    UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    //id 중복여부 확인
    public boolean checkDuplicateId(String id){
        return usersRepository.existsById(id); //JPA 기본 제공 함수 사용
    }

    public boolean signIn(String id, String password) {
        UsersEntity user;

        //id 중복 검사
        Optional<UsersEntity> optionalUser = usersRepository.findById(id);
        if(optionalUser.isPresent())
            return false;
        else{
            user = new UsersEntity();
            user.setId(id);
            user.setPw(password);
            usersRepository.save(user);
            return true;
        }
    }
}
