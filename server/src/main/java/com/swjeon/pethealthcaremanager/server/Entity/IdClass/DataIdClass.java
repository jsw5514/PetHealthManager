package com.swjeon.pethealthcaremanager.server.Entity.IdClass;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

//JPA에서의 복합 키 사용을 위한 클래스
@Getter
@Setter
@Embeddable
public class DataIdClass {
    private String uploaderId;
    private String dataId;
    
    DataIdClass(){}
    DataIdClass(String uploaderId, String dataId) {
        this.uploaderId = uploaderId;
        this.dataId = dataId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataIdClass that = (DataIdClass) o;
        return Objects.equals(uploaderId, that.uploaderId) && Objects.equals(dataId, that.dataId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uploaderId, dataId);
    }
}
