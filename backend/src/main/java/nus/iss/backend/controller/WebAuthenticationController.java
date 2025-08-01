package nus.iss.backend.controller;

import jakarta.servlet.http.HttpSession;
import nus.iss.backend.dao.LoginReqWeb;
import nus.iss.backend.dao.LoginResponseWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@CrossOrigin
@RestController
@RequestMapping("/api/web")
public class WebAuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private HttpSession session;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseWeb> authenticateDoctor(@RequestBody LoginReqWeb loginReq, HttpSession httpSession) {
        try {
            Doctor doctor = doctorService.login(loginReq.getMcrNo(), loginReq.getPassword());
            session.setAttribute("doctorMcr", doctor.getMcrNo());

            LoginResponseWeb response = new LoginResponseWeb();
            response.setMcrNo(doctor.getMcrNo());
            response.setFirstName(doctor.getFirstName());
            response.setLastName(doctor.getLastName());
            response.setEmail(doctor.getEmail());

            return new ResponseEntity<>(response, HttpStatus.OK);


        }catch (InvalidCredentialsException e) {
            logger.error("Error authenticating doctor (Status Code: " + HttpStatus.NOT_FOUND + "): " + e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }



}
