package nus.iss.backend.service;

import nus.iss.backend.dao.IntakeLogResponseWeb;
import nus.iss.backend.dao.IntakeReqMobile;
import nus.iss.backend.dao.UpdateDoctorNotesReq;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.IntakeRepository;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.Implementation.IntakeHistoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntakeHistoryServiceImplTest {

    @Mock
    private IntakeRepository intakeRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private IntakeHistoryImpl intakeHistoryService;

    private IntakeHistory testIntakeHistory;
    private Patient testPatient;
    private Doctor testDoctor;
    private Clinic testClinic;
    private Medication testMedication;
    private Schedule testSchedule;
    private IntakeReqMobile testIntakeReqMobile;
    private UpdateDoctorNotesReq testUpdateDoctorNotesReq;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(UUID.randomUUID());
        testClinic.setClinicName("Test Clinic");

        testDoctor = new Doctor();
        testDoctor.setMcrNo("M12345A");
        testDoctor.setFirstName("John");
        testDoctor.setLastName("Doe");
        testDoctor.setEmail("john.doe@example.com");
        testDoctor.setPassword("password123");
        testDoctor.setClinic(testClinic);

        testPatient = new Patient();
        testPatient.setId(UUID.randomUUID());
        testPatient.setFirstName("Jane");
        testPatient.setLastName("Smith");
        testPatient.setEmail("jane.smith@example.com");
        testPatient.setPassword("password123");
        testPatient.setNric("S1234567A");
        testPatient.setDob(LocalDate.of(1990, 1, 1));
        testPatient.setGender("Female");
        testPatient.setDoctor(testDoctor);
        testPatient.setClinic(testClinic);

        testMedication = new Medication();
        testMedication.setId(UUID.randomUUID());
        testMedication.setMedicationName("Aspirin");
        testMedication.setIntakeQuantity("100mg");
        testMedication.setFrequency(2);
        testMedication.setTiming("Morning and Evening");
        testMedication.setInstructions("Take with food");
        testMedication.setNotes("For pain relief");
        testMedication.setActive(true);

        testSchedule = new Schedule();
        testSchedule.setId(UUID.randomUUID());
        testSchedule.setMedication(testMedication);
        testSchedule.setPatient(testPatient);
        testSchedule.setScheduledTime(LocalTime.of(9, 0));
        testSchedule.setIsActive(true);

        testIntakeHistory = new IntakeHistory();
        testIntakeHistory.setId(UUID.randomUUID());
        testIntakeHistory.setPatient(testPatient);
        testIntakeHistory.setSchedule(testSchedule);
        testIntakeHistory.setLoggedDate(LocalDate.now());
        testIntakeHistory.setTaken(true);
        testIntakeHistory.setDoctorNote("Take with food");

        testIntakeReqMobile = new IntakeReqMobile();
        testIntakeReqMobile.setScheduleId(testSchedule.getId());
        testIntakeReqMobile.setPatientId(testPatient.getId());
        testIntakeReqMobile.setLoggedDate(LocalDate.now().toString());
        testIntakeReqMobile.setIsTaken(true);

        testUpdateDoctorNotesReq = new UpdateDoctorNotesReq();
        testUpdateDoctorNotesReq.setIntakeHistoryId(testIntakeHistory.getId());
        testUpdateDoctorNotesReq.setEditedNote("Updated note");
    }

    @Test
    void testCreateIntakeHistory_Success() {
        // Arrange
        when(scheduleRepository.findById(testSchedule.getId())).thenReturn(Optional.of(testSchedule));
        when(patientRepository.findById(testPatient.getId())).thenReturn(Optional.of(testPatient));
        when(intakeRepository.saveAndFlush(any(IntakeHistory.class))).thenReturn(testIntakeHistory);

        // Act
        intakeHistoryService.createIntakeHistory(testIntakeReqMobile);

        // Assert
        verify(scheduleRepository, times(1)).findById(testSchedule.getId());
        verify(patientRepository, times(1)).findById(testPatient.getId());
        verify(intakeRepository, times(1)).saveAndFlush(any(IntakeHistory.class));
    }

    @Test
    void testCreateIntakeHistory_ScheduleNotFound() {
        // Arrange
        when(scheduleRepository.findById(testSchedule.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            intakeHistoryService.createIntakeHistory(testIntakeReqMobile);
        });
        verify(scheduleRepository, times(1)).findById(testSchedule.getId());
        verify(patientRepository, never()).findById(any(UUID.class));
        verify(intakeRepository, never()).saveAndFlush(any(IntakeHistory.class));
    }

    @Test
    void testCreateIntakeHistory_PatientNotFound() {
        // Arrange
        when(scheduleRepository.findById(testSchedule.getId())).thenReturn(Optional.of(testSchedule));
        when(patientRepository.findById(testPatient.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            intakeHistoryService.createIntakeHistory(testIntakeReqMobile);
        });
        verify(scheduleRepository, times(1)).findById(testSchedule.getId());
        verify(patientRepository, times(1)).findById(testPatient.getId());
        verify(intakeRepository, never()).saveAndFlush(any(IntakeHistory.class));
    }

    @Test
    void testGetIntakeLogsForMedication_Success() {
        // Arrange
        UUID medicationId = testMedication.getId();
        List<IntakeHistory> historyList = Arrays.asList(testIntakeHistory);
        when(intakeRepository.findBySchedule_Medication_Id(medicationId)).thenReturn(historyList);

        // Act
        List<IntakeLogResponseWeb> result = intakeHistoryService.getIntakeLogsForMedication(medicationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        IntakeLogResponseWeb response = result.get(0);
        assertEquals(testIntakeHistory.getLoggedDate(), response.getLoggedDate());
        assertEquals(testSchedule.getScheduledTime(), response.getScheduledTime());
        assertEquals(testIntakeHistory.isTaken(), response.isTaken());
        assertEquals(testIntakeHistory.getDoctorNote(), response.getDoctorNotes());
        assertEquals(testSchedule.getId(), response.getScheduleId());
        assertEquals(testIntakeHistory.getId(), response.getIntakeHistoryId());
        verify(intakeRepository, times(1)).findBySchedule_Medication_Id(medicationId);
    }

    @Test
    void testGetIntakeLogsForMedication_EmptyList() {
        // Arrange
        UUID medicationId = testMedication.getId();
        when(intakeRepository.findBySchedule_Medication_Id(medicationId)).thenReturn(Collections.emptyList());

        // Act
        List<IntakeLogResponseWeb> result = intakeHistoryService.getIntakeLogsForMedication(medicationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(intakeRepository, times(1)).findBySchedule_Medication_Id(medicationId);
    }

    @Test
    void testUpdateCreateDoctorNote_Success() {
        // Arrange
        when(intakeRepository.findById(testIntakeHistory.getId())).thenReturn(Optional.of(testIntakeHistory));
        when(intakeRepository.saveAndFlush(any(IntakeHistory.class))).thenReturn(testIntakeHistory);

        // Act
        IntakeHistory result = intakeHistoryService.updateCreateDoctorNote(testUpdateDoctorNotesReq);

        // Assert
        assertNotNull(result);
        assertEquals("Updated note", result.getDoctorNote());
        verify(intakeRepository, times(1)).findById(testIntakeHistory.getId());
        verify(intakeRepository, times(1)).saveAndFlush(testIntakeHistory);
    }

    @Test
    void testUpdateCreateDoctorNote_LogNotFound() {
        // Arrange
        when(intakeRepository.findById(testIntakeHistory.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            intakeHistoryService.updateCreateDoctorNote(testUpdateDoctorNotesReq);
        });
        verify(intakeRepository, times(1)).findById(testIntakeHistory.getId());
        verify(intakeRepository, never()).saveAndFlush(any(IntakeHistory.class));
    }
}
