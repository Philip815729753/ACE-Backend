package com.ace.controller;

import com.ace.pojo.Competition;
import com.ace.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/default_rules")
    public ResponseEntity<?> scheduleFixtures(@RequestParam("competitionId") int competitionId) {
        try {
            String success = scheduleService.scheduleFixture(competitionId);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error scheduling fixtures: " + e.getMessage());
        }
    }
}