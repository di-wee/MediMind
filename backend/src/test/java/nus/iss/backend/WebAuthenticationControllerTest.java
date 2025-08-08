package nus.iss.backend;// src/test/java/nus/iss/backend/controller/WebAuthenticationControllerTest.java


import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.WebAuthenticationController;
import nus.iss.backend.dao.LoginReqWeb;
import nus.iss.backend.dao.RegistrationRequestWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.exceptions.UserAlreadyExist;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.service.ClinicService;
import nus.iss.backend.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebAuthenticationController.class)
class WebAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private ClinicService clinicService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticateDoctor_success() throws Exception {
        LoginReqWeb loginReq = new LoginReqWeb();
        loginReq.setMcrNo("M13641R");
        loginReq.setPassword("tn7kgsfzh");

        Doctor doctor = new Doctor();
        doctor.setMcrNo("M13641R");

        Mockito.when(doctorService.login(eq("M13641R"), eq("tn7kgsfzh"))).thenReturn(doctor);

        mockMvc.perform(post("/api/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticateDoctor_invalidCredentials() throws Exception {
        LoginReqWeb loginReq = new LoginReqWeb();
        loginReq.setMcrNo("S000000");
        loginReq.setPassword("wrong");

        Mockito.when(doctorService.login(any(), any()))
                .thenThrow(new InvalidCredentialsException("Invalid Credentials!"));

        mockMvc.perform(post("/api/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessionInfo_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("doctorMcr", "M13641R");

        Doctor doctor = new Doctor();
        doctor.setMcrNo("M13641R");

        Mockito.when(doctorService.findDoctorByMcrNo("M13641R")).thenReturn(doctor);

        mockMvc.perform(get("/api/web/session-info").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mcrNo").value("M13641R"));
    }

    @Test
    void getSessionInfo_unauthorized() throws Exception {
        mockMvc.perform(get("/api/web/session-info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("doctorMcr", "M13641R");

        mockMvc.perform(post("/api/web/logout").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void logout_badRequest() throws Exception {
        mockMvc.perform(post("/api/web/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_success() throws Exception {
        RegistrationRequestWeb req = new RegistrationRequestWeb();
        req.setMcrNo("M00000A");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@doe.com");
        req.setPassword("password");
        req.setClinicName("Raffles Medical Clinic");

        Doctor doctor = new Doctor();
        doctor.setMcrNo("M00000A");

        Mockito.when(doctorService.registerDoctor(any(RegistrationRequestWeb.class))).thenReturn(doctor);

        mockMvc.perform(post("/api/web/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mcrNo").value("M00000A"));
    }

    @Test
    void register_userAlreadyExist() throws Exception {
        RegistrationRequestWeb req = new RegistrationRequestWeb();

        Mockito.when(doctorService.registerDoctor(any()))
                .thenThrow(new UserAlreadyExist("Doctor is already registered!"));

        mockMvc.perform(post("/api/web/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_clinicNotFound() throws Exception {
        RegistrationRequestWeb req = new RegistrationRequestWeb();

        Mockito.when(doctorService.registerDoctor(any()))
                .thenThrow(new ItemNotFound("Clinic not found!"));

        mockMvc.perform(post("/api/web/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClinics_success() throws Exception {
        Clinic clinic = new Clinic();
        clinic.setClinicName("Raffles Medical Clinic");
        List<Clinic> clinics = List.of(clinic);

        Mockito.when(clinicService.getAllClinics()).thenReturn(clinics);

        mockMvc.perform(get("/api/web/all-clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clinicName").value("Raffles Medical Clinic"));
    }

    @Test
    void getAllClinics_noContent() throws Exception {
        Mockito.when(clinicService.getAllClinics()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/web/all-clinics"))
                .andExpect(status().isNoContent());
    }
}