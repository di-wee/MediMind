package nus.iss.backend.dto;

import java.time.LocalDateTime;

public class IntakeHistoryResponse {
    private String medicationName;
    private LocalDateTime scheduledTime;
    private LocalDateTime takenTime;
    private String status;

    public IntakeHistoryResponse(String medicationName, LocalDateTime scheduledTime, LocalDateTime takenTime, boolean isTaken) {
        this.medicationName = medicationName;
        this.scheduledTime = scheduledTime;
        this.takenTime = isTaken ? takenTime : null;
        this.status = isTaken ? "TAKEN" : "NOT_TAKEN";
    }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public LocalDateTime getTakenTime() { return takenTime; }
    public void setTakenTime(LocalDateTime takenTime) { this.takenTime = takenTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
