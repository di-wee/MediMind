package nus.iss.backend.controller;

import nus.iss.backend.dao.ScheduleFindResponse;
import nus.iss.backend.dao.ScheduleListReq;
import nus.iss.backend.dto.ScheduleResponse; // ✅ Import the Android DTO
import nus.iss.backend.exceptions.*;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    ScheduleService scheduleService;

    @PostMapping("/find")
    public ResponseEntity<List<ScheduleFindResponse>> getSchedulesByTime(@RequestBody ScheduleListReq req) {
        try {
            if (req == null) throw new BadRequestException("Request body cannot be null");
            if (req.getPatientId()==null) throw new BadRequestException("patientId is required");
            if (req.getTime() == null) throw new BadRequestException("time (HH:mm) is required");

            List<Schedule> schedules = scheduleService.findSchedulesByPatientIdandScheduledTime(
                    req.getTime(), req.getPatientId());

            if (schedules == null || schedules.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
        }catch (ItemNotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } catch (BadRequestException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } catch (RuntimeException e) {
                logger.error("Error in /find: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
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
