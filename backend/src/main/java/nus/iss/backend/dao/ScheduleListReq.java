package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ScheduleListReq {
    private UUID patientId;
    private LocalTime time;
}
