package nus.iss.backend.service;

import nus.iss.backend.model.Clinic;
import nus.iss.backend.repository.ClinicRepository;
import nus.iss.backend.service.Implementation.ClinicImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicServiceImplTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicImpl clinicService;

    private Clinic testClinic1;
    private Clinic testClinic2;

    @BeforeEach
    void setUp() {
        testClinic1 = new Clinic();
        testClinic1.setId(UUID.randomUUID());
        testClinic1.setClinicName("Test Clinic 1");

        testClinic2 = new Clinic();
        testClinic2.setId(UUID.randomUUID());
        testClinic2.setClinicName("Test Clinic 2");
    }

    @Test
    void testGetAllClinics() {
        // Arrange
        List<Clinic> expectedClinics = Arrays.asList(testClinic1, testClinic2);
        when(clinicRepository.findAll()).thenReturn(expectedClinics);

        // Act
        List<Clinic> result = clinicService.getAllClinics();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testClinic1.getId(), result.get(0).getId());
        assertEquals(testClinic2.getId(), result.get(1).getId());
        verify(clinicRepository, times(1)).findAll();
    }

    @Test
    void testGetAllClinics_EmptyList() {
        // Arrange
        when(clinicRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Clinic> result = clinicService.getAllClinics();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clinicRepository, times(1)).findAll();
    }

    @Test
    void testFindClinicByClinicName_Success() {
        // Arrange
        String clinicName = "Test Clinic 1";
        when(clinicRepository.findClinicByClinicName(clinicName)).thenReturn(testClinic1);

        // Act
        Clinic result = clinicService.findClinicByClinicName(clinicName);

        // Assert
        assertNotNull(result);
        assertEquals(testClinic1.getId(), result.getId());
        assertEquals(clinicName, result.getClinicName());
        verify(clinicRepository, times(1)).findClinicByClinicName(clinicName);
    }

    @Test
    void testFindClinicByClinicName_NotFound() {
        // Arrange
        String clinicName = "NonExistent Clinic";
        when(clinicRepository.findClinicByClinicName(clinicName)).thenReturn(null);

        // Act
        Clinic result = clinicService.findClinicByClinicName(clinicName);

        // Assert
        assertNull(result);
        verify(clinicRepository, times(1)).findClinicByClinicName(clinicName);
    }
}
