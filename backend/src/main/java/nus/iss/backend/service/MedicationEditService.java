package nus.iss.backend.service;

import nus.iss.backend.dto.EditMedicationRequest;
import org.springframework.http.ResponseEntity;

public interface MedicationEditService {
    ResponseEntity<?> processEditMedication(EditMedicationRequest req);
}
