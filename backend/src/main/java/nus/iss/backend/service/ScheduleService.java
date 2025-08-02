package nus.iss.backend.service;

import nus.iss.backend.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public interface ScheduleService {

    Boolean hasMissedDose(UUID scheduleId);

    Optional<Schedule> findScheduleById(UUID id);

}
