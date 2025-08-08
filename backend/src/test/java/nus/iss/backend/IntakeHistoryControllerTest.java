package nus.iss.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.IntakeHistoryController;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.dto.IntakeHistoryResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.service.IntakeHistoryService;
import nus.iss.backend.service.PatientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntakeHistoryController.class)
class IntakeHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntakeHistoryService intakeHistoryService;

    @MockitoBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveDoctorNotes_success() throws Exception {
        UpdateDoctorNotesReq req = new UpdateDoctorNotesReq();
        req.setIntakeHistoryId(UUID.randomUUID());
        req.setEditedNote("Note");

        IntakeHistory log = new IntakeHistory();
        log.setDoctorNote("Note");

        Mockito.when(intakeHistoryService.updateCreateDoctorNote(any(UpdateDoctorNotesReq.class))).thenReturn(log);

        mockMvc.perform(put("/api/logs/save/doctor-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorNote").value("Note"));
    }

    @Test
    void saveDoctorNotes_notFound() throws Exception {
        UpdateDoctorNotesReq req = new UpdateDoctorNotesReq();
        req.setIntakeHistoryId(UUID.randomUUID());
        req.setEditedNote("Note");

        Mockito.when(intakeHistoryService.updateCreateDoctorNote(any(UpdateDoctorNotesReq.class)))
                .thenThrow(new ItemNotFound("Not found"));

        mockMvc.perform(put("/api/logs/save/doctor-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveDoctorNotes_serverError() throws Exception {
        UpdateDoctorNotesReq req = new UpdateDoctorNotesReq();
        req.setIntakeHistoryId(UUID.randomUUID());
        req.setEditedNote("Note");

        Mockito.when(intakeHistoryService.updateCreateDoctorNote(any(UpdateDoctorNotesReq.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put("/api/logs/save/doctor-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getIntakeHistory_success() throws Exception {
        UUID patientId = UUID.randomUUID();
        IntakeHistoryResponse resp = new IntakeHistoryResponse("Med", null, null, true);

        Mockito.when(patientService.getIntakeHistoryByPatientId(eq(patientId)))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/patients/" + patientId + "/intake-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicationName").value("Med"));
    }

    @Test
    void getIntakeHistory_notFound() throws Exception {
        UUID patientId = UUID.randomUUID();

        Mockito.when(patientService.getIntakeHistoryByPatientId(eq(patientId)))
                .thenThrow(new ItemNotFound("Not found"));

        mockMvc.perform(get("/api/patients/" + patientId + "/intake-history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIntakeHistory_serverError() throws Exception {
        UUID patientId = UUID.randomUUID();

        Mockito.when(patientService.getIntakeHistoryByPatientId(eq(patientId)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/patients/" + patientId + "/intake-history"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createMedicationLog_success() throws Exception {
        IntakeReqMobile req = new IntakeReqMobile();
        req.setPatientId(UUID.randomUUID());
        req.setScheduleId(UUID.randomUUID());
        req.setLoggedDate("2024-06-01");
        req.setIsTaken(true);

        mockMvc.perform(post("/api/intakeHistory/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void createMedicationLog_serverError() throws Exception {
        IntakeReqMobile req = new IntakeReqMobile();
        req.setPatientId(UUID.randomUUID());
        req.setScheduleId(UUID.randomUUID());
        req.setLoggedDate("2024-06-01");
        req.setIsTaken(true);

        Mockito.doThrow(new RuntimeException("DB error"))
                .when(intakeHistoryService).createIntakeHistory(any(IntakeReqMobile.class));

        mockMvc.perform(post("/api/intakeHistory/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}