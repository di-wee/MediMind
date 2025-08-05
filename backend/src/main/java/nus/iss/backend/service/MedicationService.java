package nus.iss.backend.service;

import nus.iss.backend.dto.newMedicationReq;
import nus.iss.backend.dao.ImageOutput;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.model.Medication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public interface MedicationService {

    List<Medication> findAllMedications(List<UUID> medIds);

    Boolean hasMedicineMissedDose(UUID medicationId);

    Medication findMedicineById(UUID medicationId);

    Medication saveMedication(Medication medication);

    Medication createMedication(newMedicationReq req);

    // For OCR + NER integration
    ImageOutput sendToFastAPI(MultipartFile file) throws IOException;
    ResponseEntity<?> processEditMedication(EditMedicationRequest req);
}
