package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<PetEntity, String> {
    @Query(value = "SELECT * FROM PET WHERE USER_ID =:userId", nativeQuery = true)
    public List<PetEntity> findByUserId(@Param("userId") String userId);
}
