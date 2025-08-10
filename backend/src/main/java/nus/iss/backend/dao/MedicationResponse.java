package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MedicationResponse {
    private UUID id;
    private String medicationName;
    private String intakeQuantity;
    private int frequency;
    private String timing;
    private String instructions;
    private String note;
    private Boolean isActive;
}
