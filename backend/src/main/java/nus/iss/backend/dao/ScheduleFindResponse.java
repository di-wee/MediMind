package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ScheduleFindResponse {
    private UUID scheduleId;
    private LocalTime ScheduleTime;
    private boolean isActive;
    private UUID medicineId;
}
