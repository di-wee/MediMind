package nus.iss.backend.repository;

import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.scheduledTime = :scheduledTime " +
            "AND s.patient.id = :patientId " +
            "AND s.isActive = true")
    List<Schedule> findSchedulesByPatientIdandScheduledTime(LocalTime scheduledTime, UUID patientId);

    List<Schedule> findByMedicationAndIsActiveTrue(Medication medication);

    List<Schedule> findByMedicationAndIsActiveFalseAndCreationDateBefore(Medication medication, LocalDateTime cutoffDate);

    //Lewis: For Android API to fetch all active schedules for a patient — used for recurring daily view
    @Query("SELECT s FROM Schedule s WHERE s.patient.id = :patientId AND s.isActive = true ORDER BY s.scheduledTime")
    List<Schedule> findActiveSchedulesByPatientId(@Param("patientId") UUID patientId);
}
