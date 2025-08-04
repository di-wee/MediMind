package nus.iss.backend.controller;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.service.IntakeHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/logs")
public class IntakeHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(IntakeHistoryController.class);
    @Autowired
    IntakeHistoryService intakeHistoryService;

    @PutMapping("/save/doctor-notes")
    public ResponseEntity<IntakeHistory> saveDoctorNotes (@RequestBody UpdateDoctorNotesReq request) {
        try {
            IntakeHistory log = intakeHistoryService.updateCreateDoctorNote(request);
            return new ResponseEntity<>(log, HttpStatus.OK);

        }catch (ItemNotFound e) {
            logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
