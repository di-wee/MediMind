package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MissedDoseResponse {
    private String medicationName, intakeQuantity, timing, instructions, notes;
    private UUID id;
    private int frequency;
    private boolean isActive, missedDose;
 }
