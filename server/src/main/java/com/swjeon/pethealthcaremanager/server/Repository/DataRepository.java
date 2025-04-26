package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRepository extends JpaRepository<DataEntity, Integer> {
}
