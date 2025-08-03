package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class IntakeLogResponseWeb {
    private LocalDate loggedDate;
    private LocalTime scheduledTime;
    private boolean isTaken;
    private String doctorNotes;

}
