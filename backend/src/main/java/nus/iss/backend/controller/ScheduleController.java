package nus.iss.backend.controller;

import nus.iss.backend.dao.ScheduleListReq;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired
    ScheduleService scheduleService;

    @GetMapping("/find")
    public ResponseEntity<List<Schedule>> getSchedulesByTime(@RequestBody ScheduleListReq req) {
        List<Schedule> schedules = scheduleService.findSchedulesByPatientIdandScheduledTime
                (req.getTime(),req.getPatientId());
        if (schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(schedules);
    }
}

