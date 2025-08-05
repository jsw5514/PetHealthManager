package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.GoalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/recommendGoal")
    public double recommendGoal(String petId, int energyLevel) {
        return goalService.recommendGoal(petId, energyLevel);
    }
}
