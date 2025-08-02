package nus.iss.backend.service;

import nus.iss.backend.model.Patient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public interface PatientService {

    Optional<Patient> findPatientById(UUID id);
}
