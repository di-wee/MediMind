package nus.iss.backend.service.Implementation;

import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ScheduleImpl implements ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleImpl.class);

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> findSchedulesByScheduledTime(LocalDateTime scheduledTime){
        return scheduleRepository.findSchedulesByScheduledTime(scheduledTime);
    }

}
