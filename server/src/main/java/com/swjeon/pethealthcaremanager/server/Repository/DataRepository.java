package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRepository extends JpaRepository<DataEntity, DataIdClass> {
}
