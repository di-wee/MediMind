package nus.iss.backend.service;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Find all patients assigned to a doctor based on the doctor's MCR number.
     */
    List<Patient> findPatientsByDoctorMcr(String mcr);

    /**
     * Unassign a doctor from a specific patient.
     */
    boolean unassignDoctor(UUID patientId);
}
