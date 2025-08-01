package nus.iss.backend.controller;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.repositiory.DoctorRepository;
import nus.iss.backend.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nus.iss.backend.exceptions.ItemNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin
@RestController
@RequestMapping("/api/doctor")

public class DoctorController {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    @Autowired
    private DoctorService doctorService;

    @PutMapping("/update")
    public ResponseEntity<Void> updateDoctor(@RequestBody DoctorUpdateReqWeb update) {
        try{
            Doctor doctor = doctorService.updateDoctor(update);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ItemNotFound e) {
            logger.error("Doctor not found (Status Code: " + HttpStatus.NOT_FOUND + "): " + e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
}
