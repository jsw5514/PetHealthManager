package com.swjeon.pethealthcaremanager.server.Service;

import com.swjeon.pethealthcaremanager.server.Entity.PetEntity;
import com.swjeon.pethealthcaremanager.server.Repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoalService {
    private final PetRepository petRepository;

    public final int LEVEL_LOW = 0;
    public final int LEVEL_MEDIUM = 1;
    public final int LEVEL_HIGH = 2;
    public final int LEVEL_VERY_HIGH = 3;

    public GoalService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public double recommendGoal(String petId, int energyLevel) {
        PetEntity pet = petRepository.findById(petId).orElse(null);
        if (pet == null) {
            log.error("Pet not found");
            return 0;
        }

        double multiplier;
        switch (energyLevel) {
            case LEVEL_LOW:
                multiplier = 0.2;
                break;
            case LEVEL_MEDIUM:
                multiplier = 0.5;
                break;
            case LEVEL_HIGH:
                multiplier = 0.8;
                break;
            case LEVEL_VERY_HIGH:
                multiplier = 1.2;
                break;
            default:
                throw new IllegalArgumentException("Invalid energy level");
        }
        if (Integer.parseInt(pet.getAge()) >= 9) {
            multiplier /= 2;
        }
        if(multiplier < 0.2)
            multiplier = 0.2;

        double RER = 70 * Math.pow(pet.getWeight(), 0.75f);
        return RER * multiplier;
    }
}
