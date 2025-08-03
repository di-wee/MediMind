package nus.iss.backend.service.Implementation;

import nus.iss.backend.model.Medication;
import nus.iss.backend.repository.MedicationRepository;
import nus.iss.backend.service.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicationImpl implements MedicationService {
    private static final Logger logger = LoggerFactory.getLogger(MedicationImpl.class);

    @Autowired
    private MedicationRepository medicationRepo;

    @Override
    public Medication findMedicationById(UUID id) {
        Medication medication = medicationRepo.findById(id).orElse(null);
        if (medication == null) {
            logger.warn("Medication not found!");
        }
        return medication;
    }

    @Override
    public List<Medication> findAllMedications(List<UUID> medIds) {
        return medicationRepo.findAllByIdIn(medIds);
    }
}
