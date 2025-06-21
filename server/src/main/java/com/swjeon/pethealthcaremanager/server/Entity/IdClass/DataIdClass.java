package com.swjeon.pethealthcaremanager.server.Entity.IdClass;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

//JPA에서의 복합 키 사용을 위한 클래스
@Getter
@Setter
public class DataIdClass implements Serializable {
    private String uploaderId;
    private String dataId;
    private String dataType;
    
    public DataIdClass(){}
    public DataIdClass(String uploaderId, String dataId, String dataType) {
        this.uploaderId = uploaderId;
        this.dataId = dataId;
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataIdClass that = (DataIdClass) o;
        return Objects.equals(uploaderId, that.uploaderId) && Objects.equals(dataId, that.dataId) && Objects.equals(dataType, that.dataType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uploaderId, dataId, dataType);
    }
}
