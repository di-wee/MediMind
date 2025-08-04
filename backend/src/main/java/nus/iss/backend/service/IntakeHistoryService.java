package nus.iss.backend.service;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;    // DTO for patient intake history
import nus.iss.backend.model.IntakeHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public interface IntakeHistoryService {

    /**
     * Create a new intake-history entry from a mobile request.
     */
    IntakeHistory createIntakeHistory(IntakeReqMobile intakeReqMobile);

    /**
     * Retrieve all intake logs for a specific medication.
     */
    List<IntakeLogResponseWeb> getIntakeLogsForMedication(UUID medicationId);

    /**
     * Create or update doctor notes on an existing intake-history entry.
     */
    IntakeHistory updateCreateDoctorNote(UpdateDoctorNotesReq request);

    /**
     * NEW: Retrieve intake-history records for a given patient.
     * Returns a list of DTOs containing medicationName, scheduledTime, takenTime, and status.
     */
    List<IntakeHistoryResponse> getIntakeHistoryByPatientId(UUID patientId);
}
