package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.dto.IntakeHistoryResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.repository.DoctorRepository;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.repository.IntakeRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private DoctorRepository doctorRepo;

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

    /**
     * Save a new patient to the database (used for registration).
     */
    @Override
    public Patient savePatient(Patient patient) {
        return patientRepo.save(patient);
    }

    /**
     * Find a patient by email and password (used for login).
     */
    @Override
    public Optional<Patient> findPatientByEmailAndPassword(String email, String password) {
        return patientRepo.findByEmailAndPassword(email, password);
    }

    /**
     * Find all patients assigned to a doctor based on the doctor's MCR number.
     */
    @Override
    public List<Patient> findPatientsByDoctorMcr(String mcr) {
        return patientRepo.findByDoctorMcrNo(mcr);
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
        List<IntakeHistory> records = intakeRepository.findByPatient_Id(patientId);

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
    /**
     * Unassign a doctor from a patient.
     */
    @Override
    public boolean unassignDoctor(UUID patientId) {
        logger.info("Attempting to unassign doctor for patient {}", patientId);
        Optional<Patient> patientOpt = patientRepo.findById(patientId);

        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            logger.info("Found patient: {} {}", patient.getFirstName(), patient.getLastName());
            patient.setDoctor(null);
            patientRepo.saveAndFlush(patient); // ensure immediate DB update
            logger.info("Successfully unassigned doctor for patient {}", patientId);
            return true;
        } else {
            logger.warn("Patient {} not found", patientId);
            return false;
        }
    }

    @Override
    public void assignPatientToDoctor(UUID patientId, String doctorMcr) {
        Optional<Patient> optionalPatient = patientRepo.findById(patientId);
        Optional<Doctor> optionalDoctor = Optional.ofNullable(doctorRepo.findDoctorByMcrNo(doctorMcr));

        if (optionalPatient.isPresent() && optionalDoctor.isPresent()) {
            Patient patient = optionalPatient.get();
            Doctor doctor = optionalDoctor.get();

            if (patient.getClinic().getId().equals(doctor.getClinic().getId())) {
                patient.setDoctor(doctor);
                patientRepo.save(patient);
                logger.info("Assigned doctor {} to patient {}", doctorMcr, patientId);
            } else {
                throw new IllegalArgumentException("Doctor and patient are not from the same clinic.");
            }
        } else {
            throw new IllegalArgumentException("Invalid doctor or patient ID.");
        }
    }

    @Override
    public List<Patient> findUnassignedPatientsByDoctorClinic(String mcr) {
        Doctor doctor = doctorRepo.findDoctorByMcrNo(mcr);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found");
        }

        UUID clinicUuid = doctor.getClinic().getId();
        return patientRepo.findByClinic_IdAndDoctorIsNull(clinicUuid);
    }



}


