package com.swjeon.pethealthcaremanager.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    @PostMapping("/uploadData")
    public String uploadData(){
        return "not yet implemented"; //TODO not yet implemented
    }

    @PostMapping("/downloadData")
    public String downloadData(){
        return "not yet implemented"; //TODO not yet implemented
    }
}
