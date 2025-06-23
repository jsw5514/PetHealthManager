package com.swjeon.pethealthcaremanager.server.dto;

import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PetDTO {
    private String petId;
    private String userId;
    private String name;
    private String age;
    private String gender;
    private double weight;
    private String imgUrl;
    private double totalDistance;
    private double totalCalories;

    @Override
    public String toString() {
        return "PetDTO{" +
                "petId='" + petId + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", gender='" + gender + '\'' +
                ", weight=" + weight +
                ", imgUrl='" + imgUrl + '\'' +
                ", totalDistance=" + totalDistance +
                ", totalCalories=" + totalCalories +
                '}';
    }
    
    public PetEntity toEntity() {
        PetEntity petEntity = new PetEntity();
        petEntity.setPetId(petId);
        petEntity.setUserId(userId);
        petEntity.setName(name);
        petEntity.setAge(age);
        petEntity.setGender(gender);
        petEntity.setWeight(weight);
        petEntity.setImgUrl(imgUrl);
        petEntity.setTotalDistance(totalDistance);
        petEntity.setTotalCalories(totalCalories);
        return petEntity;
    }
}
