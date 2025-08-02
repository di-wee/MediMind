package nus.iss.backend.service.Implementation;

import nus.iss.backend.model.Patient;
import nus.iss.backend.repostiory.PatientRepository;
import nus.iss.backend.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientImpl implements PatientService {
    @Autowired
    PatientRepository patientRepo;

    @Override
    public Optional<Patient> findPatientById(UUID id) {
        return patientRepo.findById(id);

    }
}
