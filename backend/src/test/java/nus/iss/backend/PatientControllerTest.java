package nus.iss.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.backend.controller.PatientController;
import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.dto.RegisterPatientRequest;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.service.PatientService;
import nus.iss.backend.repository.ClinicRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private ClinicRepository clinicRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPatientById_success() throws Exception {
        UUID id = UUID.randomUUID();
        Patient patient = new Patient();
        patient.setId(id);
        Mockito.when(patientService.findPatientById(id)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patient/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void getPatientById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(patientService.findPatientById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patient/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPatientMedication_success() throws Exception {
        UUID id = UUID.randomUUID();
        List<MissedDoseResponse> meds = List.of(new MissedDoseResponse());
        Mockito.when(patientService.getPatientMedicationsWithMissedDose(id)).thenReturn(meds);

        mockMvc.perform(get("/api/patient/" + id + "/medications"))
                .andExpect(status().isOk());
    }

    @Test
    void getPatientMedication_itemNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(patientService.getPatientMedicationsWithMissedDose(id))
                .thenThrow(new nus.iss.backend.exceptions.ItemNotFound("not found"));

        mockMvc.perform(get("/api/patient/" + id + "/medications"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerPatient_success() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest();
        req.email = "a@b.com";
        req.password = "passwoord123";
        req.nric = "S123567A";
        req.firstName = "John";
        req.lastName = "Doe";
        req.gender = "M";
        req.dob = "2000-01-01";
        req.clinicId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        Mockito.when(clinicRepository.findById(req.clinicId)).thenReturn(Optional.of(clinic));
        Mockito.when(patientService.savePatient(any())).thenReturn(new Patient());

        mockMvc.perform(post("/api/patient/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerPatient_clinicNotFound() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest();
        req.clinicId = UUID.randomUUID();
        Mockito.when(clinicRepository.findById(req.clinicId)).thenReturn(Optional.empty());


    }

}