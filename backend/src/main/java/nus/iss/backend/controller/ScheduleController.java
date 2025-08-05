package nus.iss.backend.controller;

import nus.iss.backend.dao.ScheduleFindResponse;
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
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired
    ScheduleService scheduleService;

    @GetMapping("/find")
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
}

