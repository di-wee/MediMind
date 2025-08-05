package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageOutput {
    private String medicationName;
    private String intakeQuantity;
    private int frequency;
    private String instructions;
    private String notes;
}
