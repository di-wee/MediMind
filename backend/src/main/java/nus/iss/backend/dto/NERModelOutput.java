package nus.iss.backend.dto;

public class NERModelOutput {
    private String medicationName;
    private String intakeQuantity;
    private int frequency;
    private String instructions;
    private String notes;

    // === Getter and Setter for medicationName ===
    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    // === Getter and Setter for intakeQuantity ===
    public String getIntakeQuantity() {
        return intakeQuantity;
    }

    public void setIntakeQuantity(String intakeQuantity) {
        this.intakeQuantity = intakeQuantity;
    }

    // === Getter and Setter for frequency ===
    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    // === Getter and Setter for instructions ===
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    // === Getter and Setter for notes ===
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
