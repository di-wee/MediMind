package nus.iss.backend.service;

import nus.iss.backend.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public interface ScheduleService {
    public List<Schedule> findSchedulesByScheduledTime(LocalDateTime scheduledTime);
}
