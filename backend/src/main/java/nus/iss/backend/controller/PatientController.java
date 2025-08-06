package nus.iss.backend.controller;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.dto.RegisterPatientRequest;
import nus.iss.backend.dto.AssignPatientRequest;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.ClinicRepository;
import nus.iss.backend.service.PatientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    PatientService patientService;

    @Autowired
    private ClinicRepository clinicRepository;

    /**
     * GET endpoint to fetch a patient by their UUID.
     */
    @GetMapping("/patient/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable UUID id) {
        try {
            Optional<Patient> pt = patientService.findPatientById(id);
            return pt.map(patient -> new ResponseEntity<>(patient, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (RuntimeException e) {
            logger.error("Error retrieving patient details by id: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET endpoint to fetch a list of medications for a given patient,
     * including information on whether any doses were missed.
     */
    @GetMapping("/patient/{patientId}/medications")
    public ResponseEntity<List<MissedDoseResponse>> getPatientMedication(@PathVariable UUID patientId) {
        try {
            List<MissedDoseResponse> response = patientService.getPatientMedicationsWithMissedDose(patientId);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ItemNotFound e) {
            logger.error("Error retrieving patient medication: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Error retrieving patient medication: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST endpoint to register a new patient.
     * Allows identifying the clinic by either clinicId or clinicName.
     */
    @PostMapping("/patient/register")
    public ResponseEntity<Patient> registerPatient(@RequestBody RegisterPatientRequest request) {
        try {
            // Find the clinic either by ID (preferred) or by name
            Clinic clinic = null;
            if (request.clinicId != null) {
                Optional<Clinic> clinicOpt = clinicRepository.findById(request.clinicId);
                if (clinicOpt.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                clinic = clinicOpt.get();
            } else if (request.clinicName != null && !request.clinicName.isBlank()) {
                clinic = clinicRepository.findClinicByClinicName(request.clinicName);
                if (clinic == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Create new patient and set properties
            Patient newPatient = new Patient();
            newPatient.setEmail(request.email);
            newPatient.setPassword(request.password);
            newPatient.setNric(request.nric);
            newPatient.setFirstName(request.firstName);
            newPatient.setLastName(request.lastName);
            newPatient.setGender(request.gender);
            newPatient.setDob(LocalDate.parse(request.dob));
            newPatient.setClinic(clinic);

            // Save patient
            Patient savedPatient = patientService.savePatient(newPatient);
            return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            logger.error("Error registering patient: " + e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST endpoint to login a patient.
     */
    @PostMapping("/patient/login")
    public ResponseEntity<Patient> loginPatient(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            Optional<Patient> patientOpt = patientService.findPatientByEmailAndPassword(email, password);

            return patientOpt.map(patient -> new ResponseEntity<>(patient, HttpStatus.OK))
                             .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
        } catch (RuntimeException e) {
            logger.error("Error during patient login: " + e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT endpoint to update patient profile.
     * Allows updating basic details and optionally password.
     */
    @PutMapping("/patient/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable UUID id,
            @RequestBody Map<String, String> updateData) {
        try {
            // Find the patient to update
            Optional<Patient> optionalPatient = patientService.findPatientById(id);
            if (optionalPatient.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Patient patient = optionalPatient.get();

            // Update fields from request body
            if (updateData.containsKey("email")) patient.setEmail(updateData.get("email"));
            if (updateData.containsKey("nric")) patient.setNric(updateData.get("nric"));
            if (updateData.containsKey("firstName")) patient.setFirstName(updateData.get("firstName"));
            if (updateData.containsKey("lastName")) patient.setLastName(updateData.get("lastName"));
            if (updateData.containsKey("gender")) patient.setGender(updateData.get("gender"));
            if (updateData.containsKey("dob")) patient.setDob(LocalDate.parse(updateData.get("dob")));

            // Password is optional: only update if provided and non-empty
            if (updateData.containsKey("password") && updateData.get("password") != null &&
                !updateData.get("password").isBlank()) {
                patient.setPassword(updateData.get("password"));
            }

            // Save updated patient
            Patient updatedPatient = patientService.savePatient(patient);
            return new ResponseEntity<>(updatedPatient, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error updating patient: " + e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/patients/by-doctor/{mcr}")
    public ResponseEntity<List<Patient>> getPatientsByDoctor(@PathVariable String mcr) {
        try {
            List<Patient> patients = patientService.findPatientsByDoctorMcr(mcr);
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error retrieving patients for doctor MCR {}: {}", mcr, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/patients/{id}/unassign-doctor")
    public ResponseEntity<Void> unassignDoctorFromPatient(@PathVariable UUID id) {
        try {
            boolean updated = patientService.unassignDoctor(id);
            if (updated) {
                logger.info("Successfully unassigned doctor for patient {}", id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                logger.warn("Patient {} not found when trying to unassign doctor", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            logger.error("Error unassigning doctor from patient {}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/patient/{patientId}/medList")
    public ResponseEntity<List<Medication>> getMedListForPatient(@PathVariable UUID patientId) {
        try {
            List<Medication> medicationList = patientService.getPatientMedications(patientId);
            return new ResponseEntity<>(medicationList, HttpStatus.OK);
        } catch (ItemNotFound e) {
            logger.error("Patient not found: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Error retrieving patient medication: "+ e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/patients/assign")
    public ResponseEntity<Void> assignDoctorToPatient(@RequestBody AssignPatientRequest request) {
        try {
            UUID patientId = UUID.fromString(request.getPatientId());
            String doctorMcr = request.getDoctorId();  // Use as String

            patientService.assignPatientToDoctor(patientId, doctorMcr);
            logger.info("Assigned doctor {} to patient {}", doctorMcr, patientId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.warn("Assignment failed: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Error assigning doctor to patient: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/patients/unassigned/{mcr}")
    public ResponseEntity<List<Patient>> getUnassignedPatients(@PathVariable String mcr) {
        try {
            List<Patient> unassignedPatients = patientService.findUnassignedPatientsByDoctorClinic(mcr);
            return new ResponseEntity<>(unassignedPatients, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Unexpected error while fetching unassigned patients: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
