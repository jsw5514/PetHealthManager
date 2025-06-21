package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Setter;


@Entity
@Table(name = "data")
@IdClass(DataIdClass.class)
@Getter
@Setter
@NoArgsConstructor
public class DataEntity {
  @Id
  @Column(name = "UPLOADER_ID")
  private String uploaderId;
  @Id
  @Column(name = "DATA_ID")
  private String dataId;
  @Id
  @Column(name = "DATA_TYPE")
  private String dataType;
  @Column(name = "CONTENT", columnDefinition = "json")
  private String content;

  public DataDTO toDTO() {
    return new DataDTO(uploaderId, dataId, dataType, content);
  }
}
