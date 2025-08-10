package nus.iss.backend.service;

import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.PatientRepository;
import nus.iss.backend.repository.DoctorRepository;
import nus.iss.backend.service.Implementation.PatientServiceImpl;
import nus.iss.backend.service.ScheduleService;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.dao.MissedDoseResponse;
import nus.iss.backend.dto.IntakeHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Medication testMedication;
    private Schedule testSchedule;
    private Clinic testClinic;

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

        testPatient.setMedications(Arrays.asList(testMedication));
        testMedication.setSchedules(Arrays.asList(testSchedule));
    }

    @Test
    void testFindPatientById_Success() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

        // Act
        Optional<Patient> result = patientService.findPatientById(patientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(patientId, result.get().getId());
        assertEquals("Jane", result.get().getFirstName());
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void testFindPatientById_NotFound() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act
        Optional<Patient> result = patientService.findPatientById(patientId);

        // Assert
        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void testSavePatient_Success() {
        // Arrange
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        Patient result = patientService.savePatient(testPatient);

        // Assert
        assertNotNull(result);
        assertEquals(testPatient.getId(), result.getId());
        assertEquals("Jane", result.getFirstName());
        verify(patientRepository, times(1)).save(testPatient);
    }

    @Test
    void testFindPatientByEmailAndPassword_Success() {
        // Arrange
        when(patientRepository.findByEmailAndPassword("jane.smith@example.com", "password123"))
            .thenReturn(Optional.of(testPatient));

        // Act
        Optional<Patient> result = patientService.findPatientByEmailAndPassword("jane.smith@example.com", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("jane.smith@example.com", result.get().getEmail());
        verify(patientRepository, times(1)).findByEmailAndPassword("jane.smith@example.com", "password123");
    }

    @Test
    void testFindPatientByEmailAndPassword_NotFound() {
        // Arrange
        when(patientRepository.findByEmailAndPassword("invalid@example.com", "wrongpassword"))
            .thenReturn(Optional.empty());

        // Act
        Optional<Patient> result = patientService.findPatientByEmailAndPassword("invalid@example.com", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findByEmailAndPassword("invalid@example.com", "wrongpassword");
    }

    @Test
    void testFindPatientsByDoctorMcr_Success() {
        // Arrange
        List<Patient> expectedPatients = Arrays.asList(testPatient);
        when(patientRepository.findByDoctorMcrNo("M12345A")).thenReturn(expectedPatients);

        // Act
        List<Patient> result = patientService.findPatientsByDoctorMcr("M12345A");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("M12345A", result.get(0).getDoctor().getMcrNo());
        verify(patientRepository, times(1)).findByDoctorMcrNo("M12345A");
    }

    @Test
    void testGetPatientMedicationsWithMissedDose_Success() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(scheduleService.hasMissedDose(testSchedule.getId())).thenReturn(true);

        // Act
        List<MissedDoseResponse> result = patientService.getPatientMedicationsWithMissedDose(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aspirin", result.get(0).getMedicationName());
        assertTrue(result.get(0).isMissedDose());
        verify(patientRepository, times(1)).findById(patientId);
        verify(scheduleService, times(1)).hasMissedDose(testSchedule.getId());
    }

    @Test
    void testGetPatientMedicationsWithMissedDose_NoMissedDoses() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(scheduleService.hasMissedDose(testSchedule.getId())).thenReturn(false);

        // Act
        List<MissedDoseResponse> result = patientService.getPatientMedicationsWithMissedDose(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aspirin", result.get(0).getMedicationName());
        assertFalse(result.get(0).isMissedDose());
        verify(patientRepository, times(1)).findById(patientId);
        verify(scheduleService, times(1)).hasMissedDose(testSchedule.getId());
    }

    @Test
    void testGetPatientMedicationsWithMissedDose_PatientNotFound() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            patientService.getPatientMedicationsWithMissedDose(patientId);
        });
        verify(patientRepository, times(1)).findById(patientId);
        verify(scheduleService, never()).hasMissedDose(any(UUID.class));
    }

    @Test
    void testGetPatientMedicationsWithMissedDose_NoMedications() {
        // Arrange
        UUID patientId = testPatient.getId();
        testPatient.setMedications(Arrays.asList());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

        // Act
        List<MissedDoseResponse> result = patientService.getPatientMedicationsWithMissedDose(patientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(patientRepository, times(1)).findById(patientId);
        verify(scheduleService, never()).hasMissedDose(any(UUID.class));
    }

    @Test
    void testUnassignDoctor_Success() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(patientRepository.saveAndFlush(any(Patient.class))).thenReturn(testPatient);

        // Act
        boolean result = patientService.unassignDoctor(patientId);

        // Assert
        assertTrue(result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, times(1)).saveAndFlush(any(Patient.class));
    }

    @Test
    void testUnassignDoctor_PatientNotFound() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act
        boolean result = patientService.unassignDoctor(patientId);

        // Assert
        assertFalse(result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, never()).saveAndFlush(any(Patient.class));
    }

    @Test
    void testAssignPatientToDoctor_Success() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(testDoctor);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        patientService.assignPatientToDoctor(patientId, "M12345A");

        // Assert
        verify(patientRepository, times(1)).findById(patientId);
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testAssignPatientToDoctor_PatientNotFound() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.assignPatientToDoctor(patientId, "M12345A");
        });
        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testAssignPatientToDoctor_DoctorNotFound() {
        // Arrange
        UUID patientId = testPatient.getId();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findDoctorByMcrNo("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.assignPatientToDoctor(patientId, "INVALID");
        });
        verify(patientRepository, times(1)).findById(patientId);
        verify(doctorRepository, times(1)).findDoctorByMcrNo("INVALID");
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
