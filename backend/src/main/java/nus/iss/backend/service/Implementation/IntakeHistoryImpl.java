package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;    // ← DTO for patient intake history
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.IntakeRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.IntakeHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;                       // ← for combining date + time
import java.util.Collections;
import java.util.Comparator;                         // ← for sorting DTOs
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class IntakeHistoryImpl implements IntakeHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(IntakeHistoryImpl.class);

    @Autowired
    private IntakeRepository intakeRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Override
    public IntakeHistory createIntakeHistory(IntakeReqMobile intakeReqMobile) {
        Patient patient = patientRepo.findById(intakeReqMobile.getPatientId())
            .orElseThrow(() -> new ItemNotFound("Patient not found!"));
        IntakeHistory intakeHistory = new IntakeHistory();
        intakeHistory.setPatient(patient);
        intakeHistory.setLoggedDate(intakeReqMobile.getLoggedDate());
        intakeHistory.setDoctorNote("");
        intakeHistory.setTaken(intakeReqMobile.isTaken());
        intakeHistory.setSchedule(intakeReqMobile.getSchedule());
        intakeRepo.saveAndFlush(intakeHistory);
        return intakeHistory;
    }

    @Override
    public List<IntakeLogResponseWeb> getIntakeLogsForMedication(UUID medicationId) {
        List<IntakeHistory> historyList = intakeRepo.findBySchedule_Medication_Id(medicationId);

        if (historyList.isEmpty()) {
            logger.warn("No intake history for medication(" + medicationId + ").");
            return Collections.emptyList();
        }

        return historyList.stream().map(hx -> {
            IntakeLogResponseWeb dto = new IntakeLogResponseWeb();
            dto.setLoggedDate(hx.getLoggedDate());
            dto.setScheduledTime(hx.getSchedule().getScheduledTime());
            dto.setTaken(hx.isTaken());
            dto.setDoctorNotes(hx.getDoctorNote());
            dto.setScheduleId(hx.getSchedule().getId());
            dto.setIntakeHistoryId(hx.getId());
            return dto;
        }).toList();
    }

    @Override
    public IntakeHistory updateCreateDoctorNote(UpdateDoctorNotesReq request) {
        Optional<IntakeHistory> lg = intakeRepo.findById(request.getIntakeHistoryId());
        if (lg.isEmpty()) {
            throw new ItemNotFound("Log with ID(" + request.getIntakeHistoryId() + ") does not exist.");
        }
        IntakeHistory log = lg.get();
        log.setDoctorNote(request.getEditedNote());
        intakeRepo.saveAndFlush(log);
        return log;
    }

    /**
     * NEW: implements the interface’s getIntakeHistoryByPatientId(UUID) method
     * to retrieve and map all intake-history records for a patient.
     */
    @Override
    public List<IntakeHistoryResponse> getIntakeHistoryByPatientId(UUID patientId) {
        // 1. Fetch entities for this patient (make sure your IntakeRepository
        //    defines a method findByPatient_Id(UUID) or similar)
        List<IntakeHistory> records = intakeRepo.findByPatient_Id(patientId);

        // 2. Map each entity to our DTO
        List<IntakeHistoryResponse> dtoList = records.stream()
            .map(record -> {
                // Combine the logged date with the schedule’s time-of-day
                LocalDateTime scheduledDateTime =
                    record.getLoggedDate().atTime(record.getSchedule().getScheduledTime());
                boolean isTaken = record.isTaken();
                String medName = record.getSchedule().getMedication().getMedicationName();

                return new IntakeHistoryResponse(
                    medName,
                    scheduledDateTime,
                    scheduledDateTime,
                    isTaken
                );
            })
            .toList();

        // 3. Sort by scheduledTime before returning
        return dtoList.stream()
            .sorted(Comparator.comparing(IntakeHistoryResponse::getScheduledTime))
            .toList();
    }
}
