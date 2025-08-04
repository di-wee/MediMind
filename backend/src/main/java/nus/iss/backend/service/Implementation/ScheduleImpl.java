package nus.iss.backend.service.Implementation;

import nus.iss.backend.dto.ScheduleResponse; // added for Android API to use
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // if there's no intake history, then there's no missed dose
        if (historyList == null || historyList.isEmpty()) {
            return false;
        }

        // getting any missed dose for the current month
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // filter intake logs of current month/year and check if any isTaken == false
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

    @Override
    public List<Schedule> findActiveSchedulesByMedication(Medication medication) {
        return scheduleRepo.findByMedicationAndIsActiveTrue(medication);
    }

    // this is to keep DB clean, check and delete too old and inactive schedule data
    // also make sure related intake history will be deleted at same time
    @Override
    public void deleteOldInactiveSchedules(Medication medication, LocalDateTime cutoffDate) {
        List<Schedule> oldSchedules = scheduleRepo.findByMedicationAndIsActiveFalseAndCreationDateBefore(medication, cutoffDate);
        for (Schedule s : oldSchedules) {
            s.getIntakeHistory().size(); // force loading intake history
            scheduleRepo.delete(s);
        }
    }

    @Override
    public void deactivateSchedules(List<Schedule> schedules) {
        for (Schedule s : schedules) {
            s.setIsActive(false);
            scheduleRepo.save(s);
        }
    }

    @Override
    public Schedule createSchedule(Medication medication, Patient patient, LocalTime scheduledTime) {
        Schedule s = new Schedule();
        s.setMedication(medication);
        s.setPatient(patient);
        s.setScheduledTime(scheduledTime);
        s.setIsActive(true);
        s.setCreationDate(LocalDateTime.now());
        return scheduleRepo.save(s);
    }

    // For Android Android API to get all active recurring daily schedules for a patient
    @Override
    public List<ScheduleResponse> getDailyScheduleForPatient(UUID patientId) {
        List<Schedule> schedules = scheduleRepo.findActiveSchedulesByPatientId(patientId);

        // map Schedule → ScheduleResponse for Android consumption
        return schedules.stream().map(schedule -> {
            ScheduleResponse dto = new ScheduleResponse();
            dto.setScheduledTime(schedule.getScheduledTime().toString());
            dto.setMedicationName(schedule.getMedication().getMedicationName());
            dto.setQuantity(schedule.getMedication().getIntakeQuantity()); // if not available, use 1
            dto.setIsActive(schedule.getIsActive());
            return dto;
        }).collect(Collectors.toList());
    }
}
