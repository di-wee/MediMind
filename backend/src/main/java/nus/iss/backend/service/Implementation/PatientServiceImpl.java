package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

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
        // Fetch patient, throw exception if not found
        Optional<Patient> pt = patientRepo.findById(patientId);
        if (pt.isEmpty()) {
            throw new ItemNotFound("Patient not found!");
        }
        Patient patient = pt.get();

        // Retrieve medications from the patient entity
        List<Medication> medicationList = patient.getMedications();

        // Convert medications to MissedDoseResponse DTOs
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

                    // Check whether any schedule linked to this medication has a missed dose
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
}
