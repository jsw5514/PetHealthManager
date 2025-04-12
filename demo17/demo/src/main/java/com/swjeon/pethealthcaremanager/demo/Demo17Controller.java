package com.swjeon.pethealthcaremanager.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class Demo17Controller {
    private final Demo17Service demoService;

    @GetMapping("/demo17")
    public Map<String, Object> demoController() {
        return demoService.getDemoData();
    }
}
