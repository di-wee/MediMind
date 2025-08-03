package nus.iss.backend.service.Implementation;

import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleImpl implements ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleImpl.class);
    @Autowired
    ScheduleRepository scheduleRepo;

    @Override
    public Boolean hasMissedDose(UUID scheduleId) {
        Optional<Schedule> sch = findScheduleById(scheduleId);
        if (sch.isEmpty()) {
            throw new ItemNotFound("Schedule with ID("+ scheduleId+ ") does not exist!");
        }
        Schedule schedule = sch.get();
        List<IntakeHistory> historyList = schedule.getIntakeHistory();

        //if theres no intake history, then theres no missed dose
        if (historyList == null || historyList.isEmpty()) {
            return false;
        }

        // getting any missed dose for the current month
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        //so first filtering for intake logs of current month and year and checking if any of it
        //has isTaken false (means its been missed), if yes, return true
        return historyList.stream()
                .filter(hx ->
                                hx.getLoggedDate() != null &&
                                        hx.getLoggedDate().getMonthValue() == currentMonth &&
                                        hx.getLoggedDate().getYear() == currentYear
                        )
                .anyMatch(hx -> !hx.isTaken());


    }

    @Override
    public Optional<Schedule> findScheduleById(UUID id) {
        return scheduleRepo.findById(id);
    }

    @Override
    public List<Schedule> findSchedulesByScheduledTime(LocalDateTime scheduledTime){
        return scheduleRepo.findSchedulesByScheduledTime(scheduledTime);
    }
}
