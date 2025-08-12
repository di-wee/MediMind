package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;    // ← DTO for patient intake history
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.IntakeRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Override
    public void createIntakeHistory(IntakeReqMobile intakeReqMobile) {
        Schedule schedule = scheduleRepo.findById(intakeReqMobile.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        Patient patient = patientRepo.findById(intakeReqMobile.getPatientId())
            .orElseThrow(() -> new ItemNotFound("Patient not found!"));
        IntakeHistory intakeHistory = new IntakeHistory();
        intakeHistory.setPatient(patient);
        intakeHistory.setLoggedDate(LocalDate.parse(intakeReqMobile.getLoggedDate()));
        intakeHistory.setDoctorNote("");
        intakeHistory.setTaken(intakeReqMobile.getIsTaken());
        intakeHistory.setSchedule(schedule);
        intakeRepo.saveAndFlush(intakeHistory);
    }

    @Override
    public List<IntakeLogResponseWeb> getIntakeLogsForMedication(UUID medicationId) {
        List<IntakeHistory> historyList = intakeRepo.findBySchedule_Medication_Id(medicationId);

        if (historyList.isEmpty()) {
            logger.warn("No intake history for medication({}).", LogSanitizer.sanitizeForLog(medicationId.toString()));
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


}
