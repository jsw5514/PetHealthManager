package com.swjeon.pethealthcaremanager.server.controller.dashboard;

import com.swjeon.pethealthcaremanager.server.dto.DataDTO;
import com.swjeon.pethealthcaremanager.server.service.DashBoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashBoardAPIController {
    Logger log = LoggerFactory.getLogger(DashBoardAPIController.class);
    private final DashBoardService dashBoardService;

    public DashBoardAPIController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    @GetMapping("/stats")
    public List<DataDTO> stats(String userId, String petId) {
        log.info("Get stats for pet " + petId + " from user " + userId);
        return dashBoardService.findStats(userId, petId);
    }
}
