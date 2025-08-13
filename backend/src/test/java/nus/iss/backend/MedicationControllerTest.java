package nus.iss.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.MedicationController;
import nus.iss.backend.dao.*;
import nus.iss.backend.dto.EditMedicationRequest;
import nus.iss.backend.dto.newMedicationReq;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.service.MedicationService;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicationController.class)
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private IntakeHistoryService intakeHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMedications_success() throws Exception {
        MedicationIdList medIds = new MedicationIdList();
        medIds.setMedicationIds(List.of(UUID.randomUUID()));

        Medication med = new Medication();
        med.setId(medIds.getMedicationIds().get(0));
        med.setMedicationName("Panadol");
        med.setIntakeQuantity("1");
        med.setFrequency(2);
        med.setActive(true);

        Mockito.when(medicationService.findAllMedications(anyList())).thenReturn(List.of(med));

        mockMvc.perform(post("/api/medication/medList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicationName").value("Panadol"));
    }

    @Test
    void getMedications_emptyList() throws Exception {
        MedicationIdList medIds = new MedicationIdList();
        medIds.setMedicationIds(Collections.emptyList());

        mockMvc.perform(post("/api/medication/medList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medIds)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMedicationEditDetails_success() throws Exception {
        UUID medId = UUID.randomUUID();
        Medication med = new Medication();
        med.setId(medId);
        med.setFrequency(2);

        Schedule schedule = new Schedule();
        schedule.setScheduledTime(java.time.LocalTime.of(8, 0));
        Mockito.when(medicationService.findMedicineById(eq(medId))).thenReturn(med);
        Mockito.when(scheduleService.findActiveSchedulesByMedication(any())).thenReturn(List.of(schedule));

        mockMvc.perform(get("/api/medication/" + medId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frequency").value(2));
    }

    @Test
    void getMedicationEditDetails_notFound() throws Exception {
        UUID medId = UUID.randomUUID();
        Mockito.when(medicationService.findMedicineById(eq(medId))).thenReturn(null);

        mockMvc.perform(get("/api/medication/" + medId + "/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveEditMedication_success() throws Exception {
        EditMedicationRequest req = new EditMedicationRequest();
        req.setTimes(List.of("0800", "1200"));
        Mockito.when(medicationService.processEditMedication(any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/api/medication/edit/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void saveEditMedication_invalidTimeFormat() throws Exception {
        EditMedicationRequest req = new EditMedicationRequest();
        req.setTimes(List.of("8am"));

        mockMvc.perform(post("/api/medication/edit/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveEditMedication_illegalArgument() throws Exception {
        EditMedicationRequest req = new EditMedicationRequest();
        req.setTimes(List.of("0800"));
        Mockito.when(medicationService.processEditMedication(any()))
                .thenThrow(new IllegalArgumentException("Invalid"));

        mockMvc.perform(post("/api/medication/edit/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveEditMedication_serverError() throws Exception {
        EditMedicationRequest req = new EditMedicationRequest();
        req.setTimes(List.of("0800"));
        Mockito.when(medicationService.processEditMedication(any()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/medication/edit/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getMedicationLog_success() throws Exception {
        UUID medId = UUID.randomUUID();
        IntakeLogResponseWeb log = new IntakeLogResponseWeb();
        log.setDoctorNotes("note");
        Mockito.when(intakeHistoryService.getIntakeLogsForMedication(eq(medId))).thenReturn(List.of(log));

        mockMvc.perform(get("/api/medication/" + medId + "/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorNotes").value("note"));
    }

    @Test
    void getMedicationLog_serverError() throws Exception {
        UUID medId = UUID.randomUUID();
        Mockito.when(intakeHistoryService.getIntakeLogsForMedication(eq(medId)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/medication/" + medId + "/logs"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void saveMedication_success() throws Exception {
        newMedicationReq req = new newMedicationReq();
        req.setPatientId(UUID.randomUUID());
        req.setTimes(List.of("08:00"));
        Patient patient = new Patient();
        Mockito.when(patientService.findPatientById(any())).thenReturn(Optional.of(patient));
        Medication med = new Medication();
        Mockito.when(medicationService.createMedication(any())).thenReturn(med);
        Mockito.when(scheduleService.createSchedule(any(), any(), any())).thenReturn(new Schedule());

        mockMvc.perform(post("/api/medication/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void saveMedication_patientNotFound() throws Exception {
        newMedicationReq req = new newMedicationReq();
        req.setPatientId(UUID.randomUUID());
        Mockito.when(medicationService.createMedication(any())).thenThrow(new ItemNotFound("Patient not found!"));

        mockMvc.perform(post("/api/medication/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveMedication_serverError() throws Exception {
        newMedicationReq req = new newMedicationReq();
        req.setPatientId(UUID.randomUUID());
        Mockito.when(patientService.findPatientById(any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/medication/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deactivateMedication_success() throws Exception {
        UUID medId = UUID.randomUUID();
        Medication med = new Medication();
        med.setId(medId);
        med.setActive(true);
        Mockito.when(medicationService.findMedicineById(eq(medId))).thenReturn(med);
        Mockito.when(scheduleService.findActiveSchedulesByMedication(any())).thenReturn(List.of(new Schedule()));

        mockMvc.perform(put("/api/medication/" + medId + "/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateMedication_notFound() throws Exception {
        UUID medId = UUID.randomUUID();
        Mockito.when(medicationService.findMedicineById(eq(medId))).thenReturn(null);

        mockMvc.perform(put("/api/medication/" + medId + "/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void predict_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        ImageOutput output = new ImageOutput();
        output.setMedicationName("Panadol");
        Mockito.when(medicationService.sendToFastAPI(any())).thenReturn(output);

        mockMvc.perform(multipart("/api/medication/predict_image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicationName").value("Panadol"));
    }
}