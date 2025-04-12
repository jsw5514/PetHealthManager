package com.swjeon.pethealthcaremanager.demo;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Demo17Service {
    public Map<String, Object> getDemoData() {
        Map<String, Object> map = new HashMap<>();
        map.put("data","stringdata");
        return map;
    }
}
