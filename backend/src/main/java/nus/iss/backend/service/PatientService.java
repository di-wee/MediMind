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

    Optional<Patient> findPatientById(UUID id);

    List<MissedDoseResponse> getPatientMedicationsWithMissedDose(UUID patientId);
}
