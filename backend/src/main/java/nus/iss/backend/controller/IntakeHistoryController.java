package nus.iss.backend.controller;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;            // DTO for intake history
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (RuntimeException e) {
            logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());
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
            logger.error("Patient not found: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            logger.error("Error retrieving intake history: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/intakeHistory/create")
    public ResponseEntity<?> createMedicationLog(@RequestBody IntakeReqMobile medLogReqMobile) {
        logger.info("Received log: {}", medLogReqMobile);
        try{
            intakeHistoryService.createIntakeHistory(medLogReqMobile);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
