package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.Entity.IdClass.DataIdClass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "data")
@IdClass(DataIdClass.class)
@Getter
@NoArgsConstructor
public class DataEntity {
  @Id
  @Column(name = "UPLOADER_ID")
  private String uploaderId;
  @Id
  @Column(name = "DATA_ID")
  private String dataId;
  @Column(name = "META_DATA")
  private String metaData;
  @Column(name = "DATA_PATH")
  private String dataPath;

}
