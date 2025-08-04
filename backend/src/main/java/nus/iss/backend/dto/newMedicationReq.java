package nus.iss.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class newMedicationReq {
    private String medicationName;
    private UUID patientId;
    private String dosage;
    private int frequency;
    private String Timing;
    private String instructions;
    private String notes;
    private Boolean isActive;
    private List<String> times;
}
