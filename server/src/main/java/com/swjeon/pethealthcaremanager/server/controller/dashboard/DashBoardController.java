package com.swjeon.pethealthcaremanager.server.controller.dashboard;

import com.swjeon.pethealthcaremanager.server.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashBoardController {
    DashBoardService dashBoardService;
    @Autowired
    public DashBoardController(DashBoardService dashBoardService) { this.dashBoardService = dashBoardService; }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("petProfiles", dashBoardService.findProfiles());
        return "dashboard";
    }
}
