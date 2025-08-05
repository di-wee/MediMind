package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;
import nus.iss.backend.model.Schedule;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class IntakeReqMobile {
    private LocalDate loggedDate;
    private boolean isTaken;
    private UUID patientId;
    private UUID scheduleId;
}
