package nus.iss.backend.service;

import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public interface ScheduleService {
    List<Schedule> findSchedulesByPatientIdandScheduledTime(LocalTime scheduledTime, UUID patientId);

    Boolean hasMissedDose(UUID scheduleId);

    Optional<Schedule> findScheduleById(UUID id);

    List<Schedule> findActiveSchedulesByMedication(Medication medication);

    void deleteOldInactiveSchedules(Medication medication, LocalDateTime cutoffDate);

    void deactivateSchedules(List<Schedule> schedules);

    Schedule createSchedule(Medication medication, Patient patient, LocalTime scheduledTime);

}
