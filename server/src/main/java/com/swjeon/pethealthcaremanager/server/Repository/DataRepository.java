package com.swjeon.pethealthcaremanager.server.Repository;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DataRepository extends JpaRepository<DataEntity, DataIdClass> {
    @Query(value = "SELECT * FROM data " +
            "WHERE DATA_TYPE = 'running_stats' " +
            "AND UPLOADER_ID =:uploaderId " +
            "AND DATA_ID =:dataId", nativeQuery = true)
    List<DataEntity> findStatsById(@Param("uploaderId") String userId, @Param("dataId") String petId);
}
