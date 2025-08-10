package nus.iss.backend.service;

import nus.iss.backend.dto.ScheduleResponse;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.model.IntakeHistory;
import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Patient;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.ScheduleRepository;
import nus.iss.backend.service.Implementation.ScheduleImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleImpl scheduleService;

    private Schedule testSchedule;
    private Medication testMedication;
    private Patient testPatient;
    private Doctor testDoctor;
    private Clinic testClinic;
    private IntakeHistory testIntakeHistory;

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
        testSchedule.setCreationDate(LocalDateTime.now());
        testSchedule.setIntakeHistory(new ArrayList<>());

        testIntakeHistory = new IntakeHistory();
        testIntakeHistory.setId(UUID.randomUUID());
        testIntakeHistory.setSchedule(testSchedule);
        testIntakeHistory.setLoggedDate(LocalDate.now());
        testIntakeHistory.setTaken(true);
    }

    @Test
    void testHasMissedDose_ScheduleNotFound() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            scheduleService.hasMissedDose(scheduleId);
        });
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testHasMissedDose_NoIntakeHistory() {
        // Arrange
        UUID scheduleId = testSchedule.getId();
        testSchedule.setIntakeHistory(null);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Boolean result = scheduleService.hasMissedDose(scheduleId);

        // Assert
        assertFalse(result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testHasMissedDose_EmptyIntakeHistory() {
        // Arrange
        UUID scheduleId = testSchedule.getId();
        testSchedule.setIntakeHistory(Arrays.asList());
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Boolean result = scheduleService.hasMissedDose(scheduleId);

        // Assert
        assertFalse(result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testHasMissedDose_NoMissedDoses() {
        // Arrange
        UUID scheduleId = testSchedule.getId();
        testSchedule.setIntakeHistory(Arrays.asList(testIntakeHistory));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Boolean result = scheduleService.hasMissedDose(scheduleId);

        // Assert
        assertFalse(result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testHasMissedDose_WithMissedDoses() {
        // Arrange
        UUID scheduleId = testSchedule.getId();
        testIntakeHistory.setTaken(false);
        testSchedule.setIntakeHistory(Arrays.asList(testIntakeHistory));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Boolean result = scheduleService.hasMissedDose(scheduleId);

        // Assert
        assertTrue(result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testFindScheduleById_Success() {
        // Arrange
        UUID scheduleId = testSchedule.getId();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Optional<Schedule> result = scheduleService.findScheduleById(scheduleId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(scheduleId, result.get().getId());
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testFindScheduleById_NotFound() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        // Act
        Optional<Schedule> result = scheduleService.findScheduleById(scheduleId);

        // Assert
        assertFalse(result.isPresent());
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    @Test
    void testFindSchedulesByPatientIdandScheduledTime() {
        // Arrange
        LocalTime scheduledTime = LocalTime.of(9, 0);
        UUID patientId = testPatient.getId();
        List<Schedule> expectedSchedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findSchedulesByPatientIdandScheduledTime(scheduledTime, patientId))
            .thenReturn(expectedSchedules);

        // Act
        List<Schedule> result = scheduleService.findSchedulesByPatientIdandScheduledTime(scheduledTime, patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchedule.getId(), result.get(0).getId());
        verify(scheduleRepository, times(1)).findSchedulesByPatientIdandScheduledTime(scheduledTime, patientId);
    }

    @Test
    void testFindActiveSchedulesByMedication() {
        // Arrange
        List<Schedule> expectedSchedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByMedicationAndIsActiveTrue(testMedication)).thenReturn(expectedSchedules);

        // Act
        List<Schedule> result = scheduleService.findActiveSchedulesByMedication(testMedication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchedule.getId(), result.get(0).getId());
        verify(scheduleRepository, times(1)).findByMedicationAndIsActiveTrue(testMedication);
    }

    @Test
    void testDeleteOldInactiveSchedules() {
        // Arrange
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Schedule> oldSchedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByMedicationAndIsActiveFalseAndCreationDateBefore(testMedication, cutoffDate))
            .thenReturn(oldSchedules);

        // Act
        scheduleService.deleteOldInactiveSchedules(testMedication, cutoffDate);

        // Assert
        verify(scheduleRepository, times(1)).findByMedicationAndIsActiveFalseAndCreationDateBefore(testMedication, cutoffDate);
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }

    @Test
    void testDeactivateSchedules() {
        // Arrange
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Act
        scheduleService.deactivateSchedules(schedules);

        // Assert
        assertFalse(testSchedule.getIsActive());
        verify(scheduleRepository, times(1)).save(testSchedule);
    }

    @Test
    void testCreateSchedule() {
        // Arrange
        LocalTime scheduledTime = LocalTime.of(9, 0);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Act
        Schedule result = scheduleService.createSchedule(testMedication, testPatient, scheduledTime);

        // Assert
        assertNotNull(result);
        assertEquals(testMedication, result.getMedication());
        assertEquals(testPatient, result.getPatient());
        assertEquals(scheduledTime, result.getScheduledTime());
        assertTrue(result.getIsActive());
        assertNotNull(result.getCreationDate());
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    void testGetDailyScheduleForPatient() {
        // Arrange
        UUID patientId = testPatient.getId();
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findActiveSchedulesByPatientId(patientId)).thenReturn(schedules);

        // Act
        List<ScheduleResponse> result = scheduleService.getDailyScheduleForPatient(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ScheduleResponse response = result.get(0);
        assertEquals(testSchedule.getScheduledTime().toString(), response.getScheduledTime());
        assertEquals(testMedication.getMedicationName(), response.getMedicationName());
        assertEquals(testMedication.getIntakeQuantity(), response.getQuantity());
        assertEquals(testSchedule.getIsActive(), response.getIsActive());
        verify(scheduleRepository, times(1)).findActiveSchedulesByPatientId(patientId);
    }

    @Test
    void testGetDailyScheduleForPatient_WithInactiveMedication() {
        // Arrange
        UUID patientId = testPatient.getId();
        testMedication.setActive(false);
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findActiveSchedulesByPatientId(patientId)).thenReturn(schedules);

        // Act
        List<ScheduleResponse> result = scheduleService.getDailyScheduleForPatient(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Should filter out inactive medication
        verify(scheduleRepository, times(1)).findActiveSchedulesByPatientId(patientId);
    }

    @Test
    void testGetDailyScheduleForPatient_WithNullMedication() {
        // Arrange
        UUID patientId = testPatient.getId();
        testSchedule.setMedication(null);
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findActiveSchedulesByPatientId(patientId)).thenReturn(schedules);

        // Act
        List<ScheduleResponse> result = scheduleService.getDailyScheduleForPatient(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Should filter out null medication
        verify(scheduleRepository, times(1)).findActiveSchedulesByPatientId(patientId);
    }
}
