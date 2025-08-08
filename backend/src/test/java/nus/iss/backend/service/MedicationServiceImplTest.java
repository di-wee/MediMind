package nus.iss.backend.service;

import nus.iss.backend.model.Medication;
import nus.iss.backend.model.Schedule;
import nus.iss.backend.repository.MedicationRepository;
import nus.iss.backend.service.Implementation.MedicationImpl;
import nus.iss.backend.service.ScheduleService;
import nus.iss.backend.exceptions.ItemNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private MedicationImpl medicationService;

    private Medication testMedication;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testFindMedicineById_Success() {
        // Arrange
        UUID medicationId = testMedication.getId();
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.of(testMedication));

        // Act
        Medication result = medicationService.findMedicineById(medicationId);

        // Assert
        assertNotNull(result);
        assertEquals(medicationId, result.getId());
        assertEquals("Aspirin", result.getMedicationName());
        verify(medicationRepository, times(1)).findById(medicationId);
    }

    @Test
    void testFindMedicineById_NotFound() {
        // Arrange
        UUID medicationId = UUID.randomUUID();
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.empty());

        // Act
        Medication result = medicationService.findMedicineById(medicationId);

        // Assert
        assertNull(result);
        verify(medicationRepository, times(1)).findById(medicationId);
    }

    @Test
    void testFindAllMedications_Success() {
        // Arrange
        List<UUID> medicationIds = Arrays.asList(testMedication.getId());
        List<Medication> expectedMedications = Arrays.asList(testMedication);
        when(medicationRepository.findAllById(medicationIds)).thenReturn(expectedMedications);

        // Act
        List<Medication> result = medicationService.findAllMedications(medicationIds);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMedication.getId(), result.get(0).getId());
        verify(medicationRepository, times(1)).findAllById(medicationIds);
    }

    @Test
    void testSaveMedication_Success() {
        // Arrange
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        // Act
        Medication result = medicationService.saveMedication(testMedication);

        // Assert
        assertNotNull(result);
        assertEquals(testMedication.getId(), result.getId());
        assertEquals("Aspirin", result.getMedicationName());
        verify(medicationRepository, times(1)).save(testMedication);
    }

    @Test
    void testHasMedicineMissedDose_WithSchedules() {
        // Arrange
        UUID medicationId = testMedication.getId();
        testMedication.setSchedules(Arrays.asList(testSchedule));
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.of(testMedication));
        when(scheduleService.hasMissedDose(testSchedule.getId())).thenReturn(true);

        // Act
        Boolean result = medicationService.hasMedicineMissedDose(medicationId);

        // Assert
        assertTrue(result);
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(scheduleService, times(1)).hasMissedDose(testSchedule.getId());
    }

    @Test
    void testHasMedicineMissedDose_NoSchedules() {
        // Arrange
        UUID medicationId = testMedication.getId();
        testMedication.setSchedules(Arrays.asList());
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.of(testMedication));

        // Act
        Boolean result = medicationService.hasMedicineMissedDose(medicationId);

        // Assert
        assertFalse(result);
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(scheduleService, never()).hasMissedDose(any(UUID.class));
    }

    @Test
    void testHasMedicineMissedDose_NullSchedules() {
        // Arrange
        UUID medicationId = testMedication.getId();
        testMedication.setSchedules(null);
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.of(testMedication));

        // Act
        Boolean result = medicationService.hasMedicineMissedDose(medicationId);

        // Assert
        assertFalse(result);
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(scheduleService, never()).hasMissedDose(any(UUID.class));
    }

    @Test
    void testHasMedicineMissedDose_MedicationNotFound() {
        // Arrange
        UUID medicationId = UUID.randomUUID();
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            medicationService.hasMedicineMissedDose(medicationId);
        });
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(scheduleService, never()).hasMissedDose(any(UUID.class));
    }

    @Test
    void testHasMedicineMissedDose_NoMissedDoses() {
        // Arrange
        UUID medicationId = testMedication.getId();
        testMedication.setSchedules(Arrays.asList(testSchedule));
        when(medicationRepository.findById(medicationId)).thenReturn(java.util.Optional.of(testMedication));
        when(scheduleService.hasMissedDose(testSchedule.getId())).thenReturn(false);

        // Act
        Boolean result = medicationService.hasMedicineMissedDose(medicationId);

        // Assert
        assertFalse(result);
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(scheduleService, times(1)).hasMissedDose(testSchedule.getId());
    }
}
