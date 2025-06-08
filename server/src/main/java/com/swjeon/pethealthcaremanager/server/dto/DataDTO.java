package com.swjeon.pethealthcaremanager.server.dto;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*DataDTO 구조
 * 업로드 시에만 필요(NonNull,JsonIgnore 적용)
 * 없음
 * -----------------------------
 * 다운로드 시에만 필요(어노테이션 미적용)
 * 없음
 * -----------------------------
 * 업로드/다운로드 시 모두 필요(NonNull 적용)
 * String uploaderId = 업로더 id
 * String dataId = 데이터 식별자
 *                 (같은 유저가 올린 데이터 사이에서 특정 데이터를 구별하기 위함, 클라이언트 임의로 설정
 * String metaData = 데이터 종류 등 데이터에 대해 추가로 저장하고 싶은 정보
 * String data = 데이터 자체(바이너리 데이터는 base64로 인코딩하여 전송)
 */
@Getter
@Setter
@RequiredArgsConstructor
public class DataDTO {
    @NonNull
    private String uploaderId;
    @NonNull
    private String dataId;
    @NonNull
    private String metaData;
    @NonNull
    private String data;

    @Override
    public String toString() {
        return "uploaderId: " + uploaderId + ", dataId: " + dataId + ", metaData: " + metaData + ", data: " + data;
    }

    public String generateFileName() {
        return uploaderId + "_" + dataId + ".txt";
    }

    public DataEntity toEntity() {
        DataEntity dataEntity = new DataEntity();
        dataEntity.setUploaderId(uploaderId);
        dataEntity.setDataId(dataId);
        dataEntity.setMetaData(metaData);
        dataEntity.setContent(data);
        return dataEntity;
    }

    public static DataDTO fromEntity(DataEntity dataEntity) {
        return new DataDTO(dataEntity.getUploaderId(), dataEntity.getDataId(), dataEntity.getMetaData(), dataEntity.getContent());
    }
}
