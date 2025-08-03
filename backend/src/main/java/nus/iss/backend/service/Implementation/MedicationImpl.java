package nus.iss.backend.service.Implementation;

import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.MedicationRepository;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicationImpl implements MedicationService {
    private static final Logger logger = LoggerFactory.getLogger(MedicationImpl.class);

    @Autowired
    MedicationRepository medicationRepo;

    @Autowired
    ScheduleService scheduleService;

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
}
