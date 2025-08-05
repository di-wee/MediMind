package nus.iss.backend.controller;

import nus.iss.backend.dao.*;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.dto.newMedicationReq;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @PostMapping("/medList")
    public ResponseEntity<List<MedicationResponse>> getMedications(@RequestBody MedicationIdList MedIds) {
        logger.info("[POST /medList] Received medicationIdList: {}", MedIds.getMedicationIds());
        try{
            if (MedIds.getMedicationIds().isEmpty()) {
                logger.warn("Received empty medicationIdList");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            List<Medication> medications =  medicationService.findAllMedications(MedIds.getMedicationIds());
            logger.info("Found {} medications from database", medications.size());

            List<MedicationResponse> responseList = new ArrayList<>();
            for (Medication med : medications) {
                MedicationResponse res = new MedicationResponse();
                res.setId(med.getId());
                res.setMedicationName(med.getMedicationName());
                res.setIntakeQuantity(med.getIntakeQuantity());
                res.setFrequency(med.getFrequency());
                res.setTiming(med.getTiming());
                res.setInstructions(med.getInstructions());
                res.setNote(med.getNotes());
                res.setActive(med.isActive());
                logger.debug("Prepared response for medication: {} - {}", med.getId(), med.getMedicationName());
                responseList.add(res);}
            return new ResponseEntity<>(responseList,HttpStatus.OK);
        }catch (RuntimeException e) {
            logger.error("Exception occurred in /medList: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createMedLog")
    public ResponseEntity<IntakeResponseMobile> createMedicationLog(@RequestBody IntakeReqMobile medLogReqMobile) {
        try{
            intakeHistoryService.createIntakeHistory(medLogReqMobile);
            IntakeResponseMobile saveHistory = new IntakeResponseMobile();
            saveHistory.setLoggedDate(medLogReqMobile.getLoggedDate());
            saveHistory.setTaken(medLogReqMobile.isTaken());
            saveHistory.setPatientId(medLogReqMobile.getPatientId());
            saveHistory.setScheduleId(medLogReqMobile.getScheduleId());
            return new ResponseEntity<>(saveHistory,HttpStatus.OK);
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
            return medicationService.processEditMedication(req);
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

        } catch (RuntimeException e) {
            logger.error("Error in retrieving logs for medication(" + medicationId + ").");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveMedication(@RequestBody newMedicationReq req) {
        logger.info(">>> /save API hit, request received: " + req.toString());
        try{
            logger.info("📦 received notes: " + req.getNotes());

            Patient patient =  patientService.findPatientById(req.getPatientId()).orElse(null);
            if(patient==null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("patient not found");
            }
            //save new medication
            Medication med = medicationService.createMedication(req);
            logger.info("Note from request: " + med.getNotes());
            //save new schedules
            List<Schedule> schedules = new ArrayList<>();
            for (String timeStr : req.getTimes()) {
                LocalTime time = LocalTime.parse(timeStr);
                Schedule schedule = scheduleService.createSchedule(med, patient, time);
                schedules.add(schedule);
            }
            return ResponseEntity.ok("Medication saved");
        }catch (RuntimeException e) {
            logger.error("Exception occurred:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // LST: to deactivate the medication and all related schedules
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

    // Pris: prediction from the ML model
    @PostMapping("/predict")
    public ResponseEntity<ImageOutput> predict(@RequestParam("file") MultipartFile file) throws IOException {
        ImageOutput result = medicationService.sendToFastAPI(file);
        return ResponseEntity.ok(result);
    }
}
