package nus.iss.backend.controller;

import jakarta.servlet.http.HttpSession;
import nus.iss.backend.dao.LoginReqWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.ClinicService;
import nus.iss.backend.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.http.dsl.Http;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/api/web")
public class WebAuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private ClinicService clinicService;
    @Autowired
    private HttpSession session;

    @PostMapping("/login")
    public ResponseEntity<Void> authenticateDoctor(@RequestBody LoginReqWeb loginReq, HttpSession httpSession) {
        try {
            Doctor doctor = doctorService.login(loginReq.getMcrNo(), loginReq.getPassword());
            session.setAttribute("doctorMcr", doctor.getMcrNo());


            return new ResponseEntity<>(HttpStatus.OK);


        }catch (InvalidCredentialsException e) {
            logger.error("Error authenticating doctor (Status Code: " + HttpStatus.NOT_FOUND + "): " + e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/session-info")
    public ResponseEntity<Doctor> getSessionInfo(HttpSession session) {
        try{
            String mcrNo = (String) session.getAttribute("doctorMcr");
            if (mcrNo == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Doctor doc = doctorService.findDoctorByMcrNo(mcrNo);
            return new ResponseEntity<>(doc, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error retrieving session info: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

}


@PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        String mcrNo = (String) session.getAttribute("doctorMcr");
        if (mcrNo == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
}

@GetMapping("/all-clinics")
    public ResponseEntity<List<Clinic>> getAllClinics() {
        try {
            List<Clinic> clinics = clinicService.getAllClinics();
            if (clinics.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(clinics, HttpStatus.OK);

        } catch (RuntimeException e){
            logger.error("Error retrieving all clinics: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


}
