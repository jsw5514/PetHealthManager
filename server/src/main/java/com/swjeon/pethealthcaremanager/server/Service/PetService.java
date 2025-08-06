package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import com.swjeon.pethealthcaremanager.server.dto.PetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PetService {
    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }


    public void uploadPetProfile(PetDTO petDTO) {
        PetEntity entity = petDTO.toEntity();
        petRepository.save(entity);
    }

    public void removePetProfile(String petId) {
        petRepository.deleteById(petId);
    }
}
