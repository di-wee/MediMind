package nus.iss.backend.controller;

import nus.iss.backend.dao.LoginReqWeb;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.DoctorService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@CrossOrigin
@RestController
@RequestMapping("/api/web")
public class WebAuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/login")
    public ResponseEntity<Doctor> authenticateDoctor(@RequestBody LoginReqWeb) {

    }



}
