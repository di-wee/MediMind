package nus.iss.backend.controller;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.exceptions.InvalidEmailDomainException;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {
    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    @Autowired
    private DoctorService doctorService;

    // Add this constructor for debugging
    public DoctorController() {
        System.out.println("=== DoctorController LOADED SUCCESSFULLY ===");
        logger.info("DoctorController bean created");
    }

    // Add a simple test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("DoctorController is working!");
    }

    // Add a debug endpoint to check security headers
    @GetMapping("/debug-headers")
    public ResponseEntity<String> debugHeaders(HttpServletRequest request) {
        logger.info("Debug headers endpoint called");
        StringBuilder headers = new StringBuilder();
        headers.append("Security Headers Check:\n");
        headers.append("X-Frame-Options: ").append(request.getHeader("X-Frame-Options")).append("\n");
        headers.append("X-Content-Type-Options: ").append(request.getHeader("X-Content-Type-Options")).append("\n");
        headers.append("X-XSS-Protection: ").append(request.getHeader("X-XSS-Protection")).append("\n");
        headers.append("Referrer-Policy: ").append(request.getHeader("Referrer-Policy")).append("\n");
        headers.append("Content-Security-Policy: ").append(request.getHeader("Content-Security-Policy")).append("\n");
        
        return ResponseEntity.ok(headers.toString());
    }

    @PutMapping("/update")
    public ResponseEntity<Doctor> updateDoctor(@RequestBody DoctorUpdateReqWeb update) {
        logger.info("Update doctor endpoint called for MCR: {}", LogSanitizer.sanitizeForLog(update.getMcrNo()));
        try{
            Doctor doctor = doctorService.updateDoctor(update);
            logger.info("Doctor updated successfully: {}", LogSanitizer.sanitizeForLog(doctor.getMcrNo()));
            return new ResponseEntity<>(doctor, HttpStatus.OK);
        }catch (ItemNotFound e) {
            logger.error("Doctor not found (Status Code: {}): {}", HttpStatus.NOT_FOUND, LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvalidEmailDomainException e) {
            logger.error("Email domain validation failed for doctor {}: {}", LogSanitizer.sanitizeForLog(update.getMcrNo()), LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            logger.error("Unexpected error updating doctor {}: {}", LogSanitizer.sanitizeForLog(update.getMcrNo()), LogSanitizer.sanitizeForLog(e.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}