package nus.iss.backend.service.Implementation;

import jakarta.transaction.Transactional;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class MedicationEditServiceImpl implements MedicationService {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ScheduleService scheduleService;

    private final DateTimeFormatter formatterNoColon = DateTimeFormatter.ofPattern("HHmm");
    private final DateTimeFormatter formatterWithColon = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<?> processEditMedication(EditMedicationRequest req) {
        Medication med = medicationService.findMedicineById(req.getMedicationId());
        if (med == null) {
            return ResponseEntity.status(404).body("Medication not found");
        }

        Optional<Patient> patientOpt = patientService.findPatientById(req.getPatientId());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Patient not found");
        }
        Patient patient = patientOpt.get();

        //every time will clean all inactive schedules(created more than 90 days) and related intakeHistory
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        scheduleService.deleteOldInactiveSchedules(med, cutoffDate);

        //then deactivate the active schedules before create new ones
        List<Schedule> activeSchedules = scheduleService.findActiveSchedulesByMedication(med);
        scheduleService.deactivateSchedules(activeSchedules);

        //then update new frequency
        med.setFrequency(req.getFrequency());
        medicationService.saveMedication(med);

        //then create new schedules
        for (String timeStr : req.getTimes()) {
            LocalTime time;
            try {
                if (timeStr.contains(":")){
                    time = LocalTime.parse(timeStr,formatterWithColon);
                } else {
                    time = LocalTime.parse(timeStr, formatterNoColon);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid time format: " + timeStr);
            }

            scheduleService.createSchedule(med, patient, time);
        }

        //last step: reset the alarm and notification things,
        // leave to shiying to implement, probably will implement in android part, not sure

        return ResponseEntity.ok("Medication details updated successfully");
    }
}
