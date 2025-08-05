package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class IntakeResponseMobile {
    private UUID id;
    private LocalDate loggedDate;
    private boolean isTaken;
    private String doctorNote;
    private UUID patientId;
    private UUID scheduleId;
}