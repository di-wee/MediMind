package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.IntakeRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.IntakeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IntakeHistoryImpl implements IntakeHistoryService {

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
    public List<IntakeLogResponseWeb> getIntakeLogsForMedication(UUID medication) {
        return List.of();
    }
}
