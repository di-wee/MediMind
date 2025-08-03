package nus.iss.backend.controller;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.MedicationIdList;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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



}
