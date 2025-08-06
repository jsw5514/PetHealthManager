package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import com.swjeon.pethealthcaremanager.server.dto.PetDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PetService {
    private final Logger log = LoggerFactory.getLogger(PetService.class);
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
