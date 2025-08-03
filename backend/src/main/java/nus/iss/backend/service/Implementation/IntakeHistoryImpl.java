package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
        Patient patient = patientRepo.findPatientById(intakeReqMobile.getPatientId());
        if (patient == null) {
            throw new ItemNotFound("Patient not found!");
        }
        IntakeHistory intakeHistory = new IntakeHistory();
        intakeHistory.setPatient(patient);
        intakeHistory.setLoggedDate(LocalDate.now());
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
            logger.warn("No intake history for medication(" +medicationId+")." );
            return Collections.emptyList();
        }

        return historyList.stream().map(hx -> {
            IntakeLogResponseWeb dto = new IntakeLogResponseWeb();
            dto.setLoggedDate(hx.getLoggedDate());
            dto.setScheduledTime(hx.getSchedule().getScheduledTime());
            dto.setTaken(hx.isTaken());
            dto.setDoctorNotes(hx.getDoctorNote());
            return dto;
        }).toList();
    }
}
