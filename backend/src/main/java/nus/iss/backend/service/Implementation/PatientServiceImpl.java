package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Find a patient by their UUID.
     */
    @Override
    public Optional<Patient> findPatientById(UUID id) {
        return patientRepo.findById(id);
    }

    /**
     * Retrieve all medications for a given patient, including information
     * on whether any doses have been missed.
     */
    @Override
    public List<MissedDoseResponse> getPatientMedicationsWithMissedDose(UUID patientId) {
        Optional<Patient> pt = patientRepo.findById(patientId);
        if (pt.isEmpty()) {
            throw new ItemNotFound("Patient not found!");
        }
        Patient patient = pt.get();

        List<Medication> medicationList = patient.getMedications();

        return medicationList.stream()
                .map(medication -> {
                    MissedDoseResponse dto = new MissedDoseResponse();
                    dto.setId(medication.getId());
                    dto.setMedicationName(medication.getMedicationName());
                    dto.setIntakeQuantity(medication.getIntakeQuantity());
                    dto.setFrequency(medication.getFrequency());
                    dto.setInstructions(medication.getInstructions());
                    dto.setActive(medication.isActive());
                    dto.setNotes(medication.getNotes());
                    dto.setTiming(medication.getTiming());

                    boolean hasMissed = medication.getSchedules().stream()
                            .anyMatch(sch -> scheduleService.hasMissedDose(sch.getId()));

                    dto.setMissedDose(hasMissed);

                    return dto;
                }).toList();
    }

    /**
     * Save a new patient to the database (used for registration).
     */
    @Override
    public Patient savePatient(Patient patient) {
        return patientRepo.save(patient);
    }

    /**
     * Find a patient by email and password (used for login).
     */
    @Override
    public Optional<Patient> findPatientByEmailAndPassword(String email, String password) {
        return patientRepo.findByEmailAndPassword(email, password);
    }

    /**
     * Find all patients assigned to a doctor based on the doctor's MCR number.
     */
    @Override
    public List<Patient> findPatientsByDoctorMcr(String mcr) {
        return patientRepo.findByDoctorMcrNo(mcr);
    }

    /**
     * Unassign a doctor from a patient.
     */
    @Override
    public boolean unassignDoctor(UUID patientId) {
        logger.info("Attempting to unassign doctor for patient {}", patientId);
        Optional<Patient> patientOpt = patientRepo.findById(patientId);
        
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            logger.info("Found patient: {} {}", patient.getFirstName(), patient.getLastName());
            patient.setDoctor(null);
            patientRepo.saveAndFlush(patient); // ensure immediate DB update
            logger.info("Successfully unassigned doctor for patient {}", patientId);
            return true;
        } else {
            logger.warn("Patient {} not found", patientId);
            return false;
        }
    }
}