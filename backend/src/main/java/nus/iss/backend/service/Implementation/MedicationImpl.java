package nus.iss.backend.service.Implementation;

import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.MedicationRepository;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class MedicationImpl implements MedicationService {
    private static final Logger logger = LoggerFactory.getLogger(MedicationImpl.class);

    @Autowired
    MedicationRepository medicationRepo;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    PatientService patientService;

    private final DateTimeFormatter formatterNoColon = DateTimeFormatter.ofPattern("HHmm");
    private final DateTimeFormatter formatterWithColon = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Boolean hasMedicineMissedDose(UUID medicationId) {
        Medication meds = this.findMedicineById(medicationId);
        if (meds == null) {
            throw new ItemNotFound("Medication with ID(" + medicationId + ") does not exist!");
        }
        List<Schedule> scheduleList = meds.getSchedules();

        //if no schedule means theres no missed dose
        if (scheduleList == null || scheduleList.isEmpty()) {
            return false;
        }

        return scheduleList.stream()
                .anyMatch(schedule -> scheduleService.hasMissedDose(schedule.getId()));

    }


    @Override
    public Medication findMedicineById (UUID id) {
        Medication medication = medicationRepo.findById(id).orElse(null);
        if (medication == null) {
            logger.warn("Medication not found!");
        }
        return medication;
    }

    @Override
    public List<Medication> findAllMedications(List<UUID> medIds) {
        return medicationRepo.findAllById(medIds);
    }

    @Override
    public Medication saveMedication(Medication medication) {
        return medicationRepo.save(medication);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> processEditMedication(EditMedicationRequest req) {
        Medication med = this.findMedicineById(req.getMedicationId());
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
        this.saveMedication(med);

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

        //TODO:last step: reset the alarm and notification things,
        // leave to shiying to implement, probably will implement in android part, not sure

        return ResponseEntity.ok("Medication details updated successfully");
    }
}
