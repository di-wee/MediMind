package nus.iss.backend.service;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public interface PatientService {

    /**
     * Find a patient by their UUID.
     */
    Optional<Patient> findPatientById(UUID id);

    /**
     * Get all medications for a patient with missed dose info.
     */
    List<MissedDoseResponse> getPatientMedicationsWithMissedDose(UUID patientId);

    /**
     * Save a new patient to the database (for registration).
     */
    Patient savePatient(Patient patient);

    /**
     * Find a patient by email and password (for login).
     */
    Optional<Patient> findPatientByEmailAndPassword(String email, String password);

    List<Medication> getPatientMedications(UUID patientId);
}