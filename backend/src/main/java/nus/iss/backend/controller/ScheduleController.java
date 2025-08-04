package nus.iss.backend.controller;

import nus.iss.backend.dto.ScheduleResponse; // ✅ Import the Android DTO
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;

    @GetMapping("/find")
    public ResponseEntity<List<Schedule>> getSchedulesByTime(@RequestParam("time")
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                              LocalDateTime time) {
        List<Schedule> schedules = scheduleService.findSchedulesByScheduledTime(time);
        if (schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(schedules);
    }

    // ✅ [Android API] Get daily schedule for a patient (recurring daily times)
    @GetMapping("/daily/{patientId}")
    public ResponseEntity<List<ScheduleResponse>> getDailySchedule(
            @PathVariable UUID patientId
    ) {
        List<ScheduleResponse> dailySchedule = scheduleService.getDailyScheduleForPatient(patientId);
        if (dailySchedule.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(dailySchedule);
    }
}
