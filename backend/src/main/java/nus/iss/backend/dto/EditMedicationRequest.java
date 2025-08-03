package nus.iss.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EditMedicationRequest {
    private UUID medicationId;
    private UUID patientId;
    private int frequency;
    private List<String> times;
}
