package nus.iss.backend.controller;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;            // DTO for intake history
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nus.iss.backend.exceptions.*;

import java.util.List;                                       // for List<>
import java.util.UUID;                                      // for UUID
                                                                 
@CrossOrigin
@RestController
@RequestMapping("/api")                                     // Base path changed to /api
public class IntakeHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(IntakeHistoryController.class);

    @Autowired
    private IntakeHistoryService intakeHistoryService;

    @Autowired
    PatientService patientService;

    /**
     * Existing PUT endpoint for saving doctor notes on an intake log.
     */
    @PutMapping("/logs/save/doctor-notes")
    public ResponseEntity<IntakeHistory> saveDoctorNotes(@RequestBody UpdateDoctorNotesReq request) {
        try {
            IntakeHistory log = intakeHistoryService.updateCreateDoctorNote(request);
            return new ResponseEntity<>(log, HttpStatus.OK);

        } catch (ItemNotFound e) {
            logger.error("Error in retrieving log to save doctor notes: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (RuntimeException e) {
            logger.error("Error in retrieving log to save doctor notes: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * New GET endpoint to retrieve intake-history for a given patient.
     */
    @GetMapping("/patients/{patientId}/intake-history")      // Maps GET /api/patients/{patientId}/intake-history
    public ResponseEntity<List<IntakeHistoryResponse>> getIntakeHistory(
            @PathVariable("patientId") UUID patientId) {
        try {
            // Delegate to service to fetch and map entity to DTO
            List<IntakeHistoryResponse> history =
                    patientService.getIntakeHistoryByPatientId(patientId);
            return ResponseEntity.ok(history);

        } catch (ItemNotFound e) {
            logger.error("Patient not found: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            logger.error("Error retrieving intake history: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/intakeHistory/create")
    public ResponseEntity<?> createMedicationLog(@RequestBody IntakeReqMobile req) {
        logger.info("Received log: {}", LogSanitizer.sanitizeForLog(String.valueOf(req)));
        try {
            if (req == null) throw new BadRequestException("Request body cannot be null");
            if (req.getPatientId()==null) throw new BadRequestException("patientId is required");
            if (req.getMedicationId()==null) throw new BadRequestException("medicationId is required");
            if (req.getIsTaken() == null) throw new BadRequestException("isTaken is required");

            intakeHistoryService.createIntakeHistory(req);
            return ResponseEntity.ok().build();

        } catch (ItemNotFound e) {
            logger.warn("Intake not found: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            logger.warn("Intake bad request: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Intake unexpected error: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
