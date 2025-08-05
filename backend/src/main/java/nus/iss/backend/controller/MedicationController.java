package nus.iss.backend.controller;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.MedicationIdList;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/medication")
public class MedicationController {
    private static final Logger logger = LoggerFactory.getLogger(MedicationController.class);

    @Autowired
    private MedicationService medicationService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private IntakeHistoryService intakeHistoryService;
    @Autowired
    private MedicationEditService medicationEditService;

    @GetMapping("/medList")
    public ResponseEntity<List<Medication>> getMedications(@RequestBody MedicationIdList MedIds) {
        try{
            List<Medication> medications =  medicationService.findAllMedications(MedIds.getMedicationIdList());
            if (MedIds.getMedicationIdList().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(medications,HttpStatus.OK);
        }catch (RuntimeException e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @PostMapping("/createmedLog")
    public ResponseEntity<List<IntakeHistory>> createMedicationLog(@RequestBody List<IntakeReqMobile> medLogReqMobiles) {
        try{
            List<IntakeHistory> intakeHistories = new ArrayList<>();
            medLogReqMobiles.forEach(medLogReqMobile -> {
                IntakeHistory saveHistory = intakeHistoryService.createIntakeHistory(medLogReqMobile);
                intakeHistories.add(saveHistory);
            });
            return new ResponseEntity<>(intakeHistories,HttpStatus.OK);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //for my edit  med detail page to show frequency and timer
    @GetMapping("{medicationId}/edit")
    public ResponseEntity<?> getMedicationEditDetails(@PathVariable UUID medicationId) {
        Medication med = medicationService.findMedicineById(medicationId);
        if (med == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medication not found");
        }

        List<Schedule> activeSchedules = scheduleService.findActiveSchedulesByMedication(med);
        List<String> times = activeSchedules.stream()
                .map(s -> s.getScheduledTime().toString())
                .map(t -> t.length() > 5 ? t.substring(0,5) : t)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("frequency", med.getFrequency());
        result.put("activeSchedulesTimes", times);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/edit/save")
    public ResponseEntity<?> saveEditMedication(@RequestBody EditMedicationRequest req) {
        // time format validation
        for (String timeStr : req.getTimes()) {
            if (!timeStr.matches("^\\d{4}$") && !timeStr.matches("^\\d{2}:\\d{2}$")) {
                return ResponseEntity.badRequest().body("Invalid time format: " + timeStr);
            }
        }

        try {
            return medicationEditService.processEditMedication(req);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save medication details");
        }
     }



    @GetMapping("/{medicationId}/logs")
    public ResponseEntity<List<IntakeLogResponseWeb>> getMedicationLog(@PathVariable UUID medicationId) {
        try {
            List<IntakeLogResponseWeb> response = intakeHistoryService.getIntakeLogsForMedication(medicationId);
            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch (RuntimeException e) {
            logger.error("Error in retrieving logs for medication(" + medicationId + ").");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    //LST: to deactivate the medication and all related schedules
    @PutMapping("/{medicationId}/deactivate")
    public ResponseEntity<String> deactivateMedication(@PathVariable UUID medicationId) {
        Medication med = medicationService.findMedicineById(medicationId);
        if (med == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medication not found");
        }

        med.setActive(false);
        medicationService.saveMedication(med);

        List<Schedule> schedules = scheduleService.findActiveSchedulesByMedication(med);

        scheduleService.deactivateSchedules(schedules);

        return ResponseEntity.ok("Medication and related schedules deactivated successfully");
    }



}
