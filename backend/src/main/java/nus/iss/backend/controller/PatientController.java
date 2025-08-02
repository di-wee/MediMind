package nus.iss.backend.controller;

import nus.iss.backend.model.Patient;
import nus.iss.backend.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    PatientService patientService;


    @GetMapping("/patient/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable UUID id) {
        try {
            Optional<Patient> pt = patientService.findPatientById(id);

            if (pt.isPresent()) {
                Patient patient = pt.get();
                return new ResponseEntity<>(patient, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }


        } catch (RuntimeException e) {
            logger.error("Error retrieving patient details by id: "+ e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);


        }
    }
}
