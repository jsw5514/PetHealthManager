package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import com.swjeon.pethealthcaremanager.server.Repository.DataRepository;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import com.swjeon.pethealthcaremanager.server.dto.PetDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashBoardService {
    private final PetRepository petRepository;
    private DataRepository dataRepository;
    public DashBoardService(DataRepository dataRepository, PetRepository petRepository) { 
        this.dataRepository = dataRepository;
        this.petRepository = petRepository;
    }

    public List<PetDTO> findProfiles() {
        List<PetDTO> profiles = new ArrayList<>();
        for (PetEntity profileEntity : petRepository.findProfiles()) {
            profiles.add(profileEntity.toDTO());
        }
        return profiles;
    }

    public List<DataDTO> findStats(String userId, String petId) {
        List<DataEntity> statEntities = dataRepository.findStatsById(userId, petId);
        ArrayList<DataDTO> stats = new ArrayList<>();
        for (DataEntity statEntity : statEntities) {
            stats.add(statEntity.toDTO());
        }
        return stats;
    }
}
