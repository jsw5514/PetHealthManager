package com.swjeon.pethealthcaremanager.server.service;

import com.swjeon.pethealthcaremanager.server.Entity.DataEntity;
import com.swjeon.pethealthcaremanager.server.Repository.DataRepository;
import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashBoardService {
    private DataRepository dataRepository;
    public DashBoardService(DataRepository dataRepository) { this.dataRepository = dataRepository; }

    public List<DataDTO> findProfiles() {
        List<DataDTO> profiles = new ArrayList<>();
        for (DataEntity profileEntity : dataRepository.findProfiles()) {
            profiles.add(profileEntity.toDTO());
        }
        return profiles;
    }
}
