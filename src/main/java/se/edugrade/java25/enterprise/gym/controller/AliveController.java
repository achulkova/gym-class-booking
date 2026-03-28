package se.edugrade.java25.enterprise.gym.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AliveController {

    @GetMapping("/alive")
    public ResponseEntity<Map<String, String>> alive() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
