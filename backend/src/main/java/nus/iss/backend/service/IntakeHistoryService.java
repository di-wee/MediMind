package nus.iss.backend.service;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.model.IntakeHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public interface IntakeHistoryService {
    IntakeHistory createIntakeHistory(IntakeReqMobile intakeReqMobile);

    List<IntakeLogResponseWeb> getIntakeLogsForMedication(UUID medication);
}
