package nus.iss.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.ScheduleController;
import nus.iss.backend.dao.ScheduleFindResponse;
import nus.iss.backend.dao.ScheduleListReq;
import nus.iss.backend.dto.ScheduleResponse;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getSchedulesByTime_success() throws Exception {
        UUID patientId = UUID.randomUUID();
        LocalTime time = LocalTime.of(8, 0);
        Schedule schedule = new Schedule();
        schedule.setId(UUID.randomUUID());
        schedule.setScheduledTime(time);
        Medication med = new Medication();
        med.setId(UUID.randomUUID());
        schedule.setMedication(med);
        schedule.setIsActive(true);

        Mockito.when(scheduleService.findSchedulesByPatientIdandScheduledTime(eq(time), eq(patientId)))
                .thenReturn(List.of(schedule));

        ScheduleListReq req = new ScheduleListReq();
        req.setPatientId(patientId);
        req.setTime(time);

        mockMvc.perform(post("/api/schedule/find")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scheduleId").value(schedule.getId().toString()))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].medicineId").value(med.getId().toString()));
    }

    @Test
    void getSchedulesByTime_noContent() throws Exception {
        UUID patientId = UUID.randomUUID();
        LocalTime time = LocalTime.of(8, 0);

        Mockito.when(scheduleService.findSchedulesByPatientIdandScheduledTime(eq(time), eq(patientId)))
                .thenReturn(List.of());

        ScheduleListReq req = new ScheduleListReq();
        req.setPatientId(patientId);
        req.setTime(time);

        mockMvc.perform(post("/api/schedule/find")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getDailySchedule_success() throws Exception {
        UUID patientId = UUID.randomUUID();
        ScheduleResponse resp = new ScheduleResponse();
        resp.setMedicationName("Panadol");
        resp.setScheduledTime("08:00");
        resp.setQuantity("1");
        resp.setIsActive(true);

        Mockito.when(scheduleService.getDailyScheduleForPatient(eq(patientId)))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/schedule/daily/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicationName").value("Panadol"))
                .andExpect(jsonPath("$[0].scheduledTime").value("08:00"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getDailySchedule_noContent() throws Exception {
        UUID patientId = UUID.randomUUID();
        Mockito.when(scheduleService.getDailyScheduleForPatient(eq(patientId)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/schedule/daily/" + patientId))
                .andExpect(status().isNoContent());
    }
}