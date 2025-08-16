package nus.iss.backend.controller;

import nus.iss.backend.dao.*;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.dto.newMedicationReq;
import nus.iss.backend.exceptions.InvalidTimeFormatException;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.exceptions.DuplicationException;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import nus.iss.backend.util.LogSanitizer;
import nus.iss.backend.exceptions.BadRequestException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            if (MedIds == null || MedIds.getMedicationIds() == null || MedIds.getMedicationIds().isEmpty()) {
                throw new BadRequestException("Medication ID list cannot be null or empty");
            }

            List<Medication> medications =  medicationService.findAllMedications(MedIds.getMedicationIds());
            if (medications == null || medications.isEmpty()) {
                throw new ItemNotFound("No medications found for the provided IDs");
            }
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
                res.setIsActive(med.isActive());
                logger.debug("Prepared response for medication: {} - {}", med.getId(), med.getMedicationName());
                responseList.add(res);}
            return new ResponseEntity<>(responseList,HttpStatus.OK);
        }catch (ItemNotFound e) {
            logger.warn("medList not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        } catch (BadRequestException e) {
            logger.warn("medList bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(List.of());
        }catch (RuntimeException e) {
            logger.error("Exception occurred in /medList: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //for my edit  med detail page to show frequency and timer
    @GetMapping("{medicationId}/edit")
    public ResponseEntity<?> getMedicationEditDetails(@PathVariable UUID medicationId) {
        try {
            Medication med = medicationService.findMedicineById(medicationId);
            if (med == null) {
                throw new ItemNotFound("Medication not found");
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
        } catch (ItemNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving medication details");
        }
    }

    @PostMapping("/edit/save")
    public ResponseEntity<?> saveEditMedication(@RequestBody EditMedicationRequest req) {
        try {
            return medicationService.processEditMedication(req);
        } catch (InvalidTimeFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ItemNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
            logger.error("Error in retrieving logs for medication({}).", LogSanitizer.sanitizeForLog(medicationId.toString()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveMedication(@RequestBody newMedicationReq req) {
        logger.info(">>> /save API hit, request received: {}", LogSanitizer.sanitizeForLog(req.toString()));
        try{
            if (req == null) throw new BadRequestException("Request body cannot be null");
            if (req.getPatientId() == null) throw new BadRequestException("patientId is required");
            if (isBlank(req.getMedicationName())) throw new BadRequestException("medicationName is required");
            if (req.getTimes() == null || req.getTimes().isEmpty()) {
                throw new BadRequestException("At least one time is required");
            }
            logger.info("received notes: {}", LogSanitizer.sanitizeForLog(req.getNotes()));

            //save new medication
            Medication med = medicationService.createMedication(req);
            logger.info("Note from request: {}", LogSanitizer.sanitizeForLog(med.getNotes()));
            
            //save new schedules
            List<Schedule> schedules = new ArrayList<>();
            for (String timeStr : req.getTimes()) {
                if (isBlank(timeStr)) throw new BadRequestException("time value cannot be blank");
                final LocalTime time;
                try {
                    time = LocalTime.parse(timeStr);
                } catch (DateTimeParseException ex) {
                    throw new BadRequestException("Invalid time format (expect HH:mm): " + timeStr);
                }
                Schedule schedule = scheduleService.createSchedule(med, patientService.findPatientById(req.getPatientId()).get(), time);
                schedules.add(schedule);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Medication saved");

        }catch (ItemNotFound e) {
            logger.error("Error when saving medication: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (DuplicationException e) {
            logger.error("Error when saving medication: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (BadRequestException e) {
            logger.error("Error when saving medication (bad request): {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error when saving medication: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while saving medication");
        }
    }

    // LST: to deactivate the medication and all related schedules
    @PutMapping("/{medicationId}/deactivate")
    public ResponseEntity<String> deactivateMedication(@PathVariable UUID medicationId) {
        try {
            Medication med = medicationService.findMedicineById(medicationId);
            if (med == null) {
                throw new ItemNotFound("Medication not found");
            }

            med.setActive(false);
            medicationService.saveMedication(med);

            List<Schedule> schedules = scheduleService.findActiveSchedulesByMedication(med);

            scheduleService.deactivateSchedules(schedules);

            return ResponseEntity.ok("Medication and related schedules deactivated successfully");
        } catch (ItemNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deactivating medication");
        }
    }

    // Pris: prediction from the ML model
    @PostMapping(value = "/predict_image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageOutput> predict(@RequestParam("file") MultipartFile file) throws IOException {
        ImageOutput result = medicationService.sendToFastAPI(file);
        return ResponseEntity.ok(result);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
