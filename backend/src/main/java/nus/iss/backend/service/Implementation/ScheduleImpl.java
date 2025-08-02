package nus.iss.backend.service.Implementation;

import nus.iss.backend.model.Schedule;
import nus.iss.backend.repostiory.ScheduleRepository;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleImpl implements ScheduleService {

    @Autowired
    ScheduleRepository scheduleRepo;

    @Override
    public Boolean hasMissedDose(UUID scheduleId) {
        Optional<Schedule> sch = findScheduleById(scheduleId);
        return null;
    }

    @Override
    public Optional<Schedule> findScheduleById(UUID id) {
        return scheduleRepo.findById(id);
    }
}
