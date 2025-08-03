package nus.iss.backend.service;

import nus.iss.backend.model.Medication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public interface MedicationService {

    Boolean hasMedicineMissedDose(UUID medicationId);

    Medication findMedicineById (UUID medicationId);
}
