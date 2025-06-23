package com.swjeon.pethealthcaremanager.server.Entity;


import com.swjeon.pethealthcaremanager.server.dto.PetDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
public class PetEntity {
  @Id
  @Column(name = "PET_ID")
  private String petId;
  @Column(name = "USER_ID")
  private String userId;
  @Column(name = "NAME_")
  private String name;
  @Column(name = "AGE")
  private String age;
  @Column(name = "GENDER")
  private String gender;
  @Column(name = "WEIGHT")
  private double weight;
  @Column(name = "IMG_URL")
  private String imgUrl;
  @Column(name = "TOTAL_DISTANCE")
  private double totalDistance;
  @Column(name = "TOTAL_CALORIES")
  private double totalCalories;
  
  public PetDTO toDTO() {
    PetDTO petDTO = new PetDTO(petId,userId,name,age,gender,weight,imgUrl,totalDistance,totalCalories);
    return petDTO;
  }
}
