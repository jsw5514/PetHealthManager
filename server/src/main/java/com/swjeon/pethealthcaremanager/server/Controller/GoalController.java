package com.swjeon.pethealthcaremanager.server.Controller;

import com.swjeon.pethealthcaremanager.server.Service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/goal"))
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<Double> recommendGoal(@RequestParam String petId, @RequestParam int energyLevel) {
        double recommendation = goalService.recommendGoal(petId, energyLevel);
        return ResponseEntity.ok(recommendation);
    }
}
