package nus.iss.backend.service;

import nus.iss.backend.model.Medication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public interface MedicationService {
    public Medication findMedicationById(UUID id);
    public List<Medication> findAllMedications(List<UUID> medIds);
}
