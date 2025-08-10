package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class IntakeReqMobile {
    private UUID medicationId;
    private String loggedDate;
    private Boolean isTaken;
    private UUID patientId;
    private UUID scheduleId;
    private String clientRequestId;
    @Override
    public String toString() {
        return "IntakeReqMobile {" +
                "\n  medicationId = '" + medicationId + '\'' +
                ",\n  scheduleId = '" + scheduleId + '\'' +
                ",\n  patientId = '" + patientId + '\'' +
                ",\n  loggedDate = " + loggedDate +
                ",\n  isTaken = " + isTaken +
                ",\n  clientRequestId = " + clientRequestId +
                "\n}";
    }
}
