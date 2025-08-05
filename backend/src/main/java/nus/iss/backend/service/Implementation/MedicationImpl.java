package nus.iss.backend.service.Implementation;

import nus.iss.backend.dto.newMedicationReq;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.dao.ImageOutput;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.MedicationRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import nus.iss.backend.util.ImageToApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class MedicationImpl implements MedicationService {
    private static final Logger logger = LoggerFactory.getLogger(MedicationImpl.class);

    @Autowired
    MedicationRepository medicationRepo;

    @Autowired
    PatientRepository patientRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    PatientService patientService;

    private final DateTimeFormatter formatterNoColon = DateTimeFormatter.ofPattern("HHmm");
    private final DateTimeFormatter formatterWithColon = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Boolean hasMedicineMissedDose(UUID medicationId) {
        Medication meds = this.findMedicineById(medicationId);
        if (meds == null) {
            throw new ItemNotFound("Medication with ID(" + medicationId + ") does not exist!");
        }
        List<Schedule> scheduleList = meds.getSchedules();

        // if no schedule means theres no missed dose
        if (scheduleList == null || scheduleList.isEmpty()) {
            return false;
        }

        return scheduleList.stream()
                .anyMatch(schedule -> scheduleService.hasMissedDose(schedule.getId()));

    }

    @Override
    public Medication findMedicineById(UUID id) {
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

    @Override
    public Medication saveMedication(Medication medication) {
        return medicationRepo.save(medication);
    }
    

    @Override
    public ImageOutput sendToFastAPI(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        InputStreamResource fileResource = new ImageToApi(
                file.getInputStream(), file.getOriginalFilename());
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String fastapiUrl = "http://0.0.0.0:8000/predict_image"; // Replace with your local FastAPI IP

        ResponseEntity<String> response = restTemplate.postForEntity(fastapiUrl, requestEntity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode extracted = root.get("extracted_info");

        ImageOutput result = new ImageOutput();
        result.setMedicationName(extracted.path("medication_name").asText(""));
        result.setIntakeQuantity(extracted.path("intake_quantity").asText(""));
        result.setFrequency(Integer.parseInt(extracted.path("frequency").asText("")));
        result.setInstructions(extracted.path("instruction").asText(""));
        result.setNotes(extracted.path("note").asText(""));

        return result;
    }


    //LST: all the steps will work like "all or nothing"
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> processEditMedication(EditMedicationRequest req) {
        Medication med = this.findMedicineById(req.getMedicationId());
        if (med == null) {
            return ResponseEntity.status(404).body("Medication not found");
        }

        Optional<Patient> patientOpt = patientService.findPatientById(req.getPatientId());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Patient not found");
        }
        Patient patient = patientOpt.get();

        //every time will clean all inactive schedules(created more than 90 days) and related intakeHistory
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        scheduleService.deleteOldInactiveSchedules(med, cutoffDate);

        //then deactivate the active schedules before create new ones
        List<Schedule> activeSchedules = scheduleService.findActiveSchedulesByMedication(med);
        scheduleService.deactivateSchedules(activeSchedules);

        //then update new frequency
        med.setFrequency(req.getFrequency());
        this.saveMedication(med);

        //then create new schedules
        //the formatters are for HHMM and HH:MM
        for (String timeStr : req.getTimes()) {
            LocalTime time;
            try {
                if (timeStr.contains(":")){
                    time = LocalTime.parse(timeStr,formatterWithColon);
                } else {
                    time = LocalTime.parse(timeStr, formatterNoColon);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid time format: " + timeStr);
            }
            scheduleService.createSchedule(med, patient, time);
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public Medication createMedication(newMedicationReq req){
        Patient patient = patientRepo.findPatientById(req.getPatientId());
        if(patient==null) {
            logger.warn("Patient not found!");
        }
        //save new medication
        List<Patient> patients = new ArrayList<>();
        patients.add(patient);
        Medication med = new Medication();
        med.setMedicationName(req.getMedicationName());
        med.setActive(true);
        med.setIntakeQuantity(req.getDosage());
        med.setFrequency(req.getFrequency());
        med.setNotes(req.getNotes());
        med.setTiming(req.getTiming());
        med.setInstructions(req.getInstructions());
        med.setPatients(patients);
        medicationRepo.save(med);
        return med;
    }
}
