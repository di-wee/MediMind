package nus.iss.backend.controller;

import nus.iss.backend.dao.ScheduleFindResponse;
import nus.iss.backend.dao.ScheduleListReq;
import nus.iss.backend.dto.ScheduleResponse; // ✅ Import the Android DTO
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;

    @PostMapping("/find")
    public ResponseEntity<List<ScheduleFindResponse>> getSchedulesByTime(@RequestBody ScheduleListReq req) {
        List<Schedule> schedules = scheduleService.findSchedulesByPatientIdandScheduledTime
                (req.getTime(),req.getPatientId());
        if (schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<ScheduleFindResponse> responseList = new ArrayList<>();

        for (Schedule s : schedules) {
            ScheduleFindResponse res = new ScheduleFindResponse();
            res.setScheduleId(s.getId());
            res.setScheduleTime(s.getScheduledTime());
            res.setActive(s.getIsActive());
            res.setMedicineId(s.getMedication().getId());
            responseList.add(res);
        }
        return ResponseEntity.ok(responseList);
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
