package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;
import nus.iss.backend.model.Schedule;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class saveEditMedResponse {
    private List<UUID> deActivatedIds;
    private List<Schedule> newSchedules;
}
