package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;
import nus.iss.backend.model.Medication;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class saveNewMedResponse {
    private UUID ScheduleId;
    private String time;
    private UUID medId;
    private String medicationName;
}
