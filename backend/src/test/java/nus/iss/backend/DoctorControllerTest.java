package nus.iss.backend;
import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.DoctorController;
import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)

class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEndpoint_success() throws Exception {
        mockMvc.perform(get("/api/doctor/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("DoctorController is working!"));
    }

    @Test
    void updateDoctor_success() throws Exception {
        DoctorUpdateReqWeb updateReq = new DoctorUpdateReqWeb();
        updateReq.setMcrNo("M12345A");
        // set other fields as needed

        Doctor doctor = new Doctor();
        doctor.setMcrNo("M12345A");
        // set other fields as needed

        Mockito.when(doctorService.updateDoctor(any(DoctorUpdateReqWeb.class))).thenReturn(doctor);

        mockMvc.perform(put("/api/doctor/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mcrNo").value("M12345A"));
    }

    @Test
    void updateDoctor_notFound() throws Exception {
        DoctorUpdateReqWeb updateReq = new DoctorUpdateReqWeb();
        updateReq.setMcrNo("M0000000");

        Mockito.when(doctorService.updateDoctor(any(DoctorUpdateReqWeb.class)))
                .thenThrow(new ItemNotFound("Doctor not found!"));

        mockMvc.perform(put("/api/doctor/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateDoctor_nullRequestBody() throws Exception {
        mockMvc.perform(put("/api/doctor/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }


}