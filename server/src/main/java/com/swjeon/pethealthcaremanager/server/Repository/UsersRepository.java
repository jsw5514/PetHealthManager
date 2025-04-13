package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UsersEntity,String> {
    UsersEntity findUserById(String id);
}
