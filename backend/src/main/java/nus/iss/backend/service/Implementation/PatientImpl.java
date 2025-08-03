package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repostiory.PatientRepository;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientImpl implements PatientService {
    @Autowired
    PatientRepository patientRepo;
    @Autowired
    private ScheduleService scheduleService;

    @Override
    public Optional<Patient> findPatientById(UUID id) {
        return patientRepo.findById(id);

    }

    @Override
    public List<MissedDoseResponse> getPatientMedicationsWithMissedDose(UUID patientId) {
        Optional<Patient> pt = patientRepo.findById(patientId);
        if (pt.isEmpty()) {
            throw new ItemNotFound("Patient not found!");
        }
        Patient patient = pt.get();

        List<Medication> medicationList = patient.getMedications();

        return medicationList.stream()
                .map(medication -> {
                    //mapping medication into the new DTO Response format that includes missedDose
                    MissedDoseResponse dto = new MissedDoseResponse();
                    dto.setId(medication.getId());
                    dto.setMedicationName(medication.getMedicationName());
                    dto.setIntakeQuantity(medication.getIntakeQuantity());
                    dto.setFrequency(medication.getFrequency());
                    dto.setInstructions(medication.getInstructions());
                    dto.setActive(medication.isActive());
                    dto.setNotes(medication.getNotes());
                    dto.setTiming(medication.getTiming());

                    //check missed dose using schedules
                    boolean hasMissed = medication.getSchedules().stream()
                            .anyMatch(sch -> scheduleService.hasMissedDose(sch.getId()));

                    dto.setMissedDose(hasMissed);

                    return dto;

                }).toList();


    }


}
