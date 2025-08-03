package nus.iss.backend.service;

import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.model.IntakeHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface IntakeHistoryService {
    public IntakeHistory createIntakeHistory(IntakeReqMobile intakeReqMobile);
}
