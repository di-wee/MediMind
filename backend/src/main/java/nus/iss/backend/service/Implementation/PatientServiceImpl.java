package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.dto.IntakeHistoryResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.IntakeHistoryRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private IntakeHistoryRepository intakeHistoryRepository;

    @Override
    public Optional<Patient> findPatientById(UUID id) {
        return patientRepo.findById(id);
    }

    @Override
    public List<MissedDoseResponse> getPatientMedicationsWithMissedDose(UUID patientId) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ItemNotFound("Patient not found!"));

        return patient.getMedications().stream()
            .map(med -> {
                MissedDoseResponse dto = new MissedDoseResponse();
                dto.setId(med.getId());
                dto.setMedicationName(med.getMedicationName());
                dto.setIntakeQuantity(med.getIntakeQuantity());
                dto.setFrequency(med.getFrequency());
                dto.setInstructions(med.getInstructions());
                dto.setActive(med.isActive());
                dto.setNotes(med.getNotes());
                dto.setTiming(med.getTiming());

                boolean hasMissed = med.getSchedules().stream()
                    .anyMatch(sch -> scheduleService.hasMissedDose(sch.getId()));
                dto.setMissedDose(hasMissed);

                return dto;
            })
            .toList();
    }

    @Override
    public Patient savePatient(Patient patient) {
        return patientRepo.save(patient);
    }

    @Override
    public Optional<Patient> findPatientByEmailAndPassword(String email, String password) {
        return patientRepo.findByEmailAndPassword(email, password);
    }

    @Override
    public List<Medication> getPatientMedications(UUID patientId) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ItemNotFound("Patient not found!"));
        return patient.getMedications();
    }

    @Override
    public List<IntakeHistoryResponse> getIntakeHistoryByPatientId(UUID patientId) {
        // 1. Load all history entries
        List<IntakeHistory> records = intakeHistoryRepository.findByPatientId(patientId);

        // 2. Map each to a DTO, combining loggedDate + scheduledTime into a LocalDateTime
        List<IntakeHistoryResponse> dtoList = records.stream()
            .map(record -> {
                // Build a LocalDateTime from the date the record was logged and the scheduled time-of-day
                LocalDateTime scheduledDateTime =
                    record.getLoggedDate().atTime(record.getSchedule().getScheduledTime());

                boolean isTaken = record.isTaken();

                String medName = record.getSchedule()
                    .getMedication()
                    .getMedicationName();

                return new IntakeHistoryResponse(
                    medName,
                    scheduledDateTime,
                    scheduledDateTime,
                    isTaken
                );
            })
            .toList();

        // 3. Finally, sort by that scheduledDateTime and return
        return dtoList.stream()
            .sorted(Comparator.comparing(IntakeHistoryResponse::getScheduledTime))
            .toList();
    }
}
