package nus.iss.backend.service;

import nus.iss.backend.model.Doctor;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.repository.DoctorRepository;
import nus.iss.backend.repository.ClinicRepository;
import nus.iss.backend.service.Implementation.DoctorImpl;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.exceptions.UserAlreadyExist;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.RegistrationRequestWeb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private DoctorImpl doctorService;

    private Doctor testDoctor;
    private Clinic testClinic;
    private DoctorUpdateReqWeb updateRequest;
    private RegistrationRequestWeb registrationRequest;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(java.util.UUID.randomUUID());
        testClinic.setClinicName("Test Clinic");

        testDoctor = new Doctor();
        testDoctor.setMcrNo("M12345A");
        testDoctor.setFirstName("John");
        testDoctor.setLastName("Doe");
        testDoctor.setEmail("john.doe@example.com");
        testDoctor.setPassword("password123");
        testDoctor.setClinic(testClinic);

        updateRequest = new DoctorUpdateReqWeb();
        updateRequest.setMcrNo("M12345A");
        updateRequest.setEmail("jane.smith@example.com");

        registrationRequest = new RegistrationRequestWeb();
        registrationRequest.setMcrNo("M12345A");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setClinicName("Test Clinic");
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNoAndPassword("M12345A", "password123"))
            .thenReturn(testDoctor);

        // Act
        Doctor result = doctorService.login("M12345A", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("M12345A", result.getMcrNo());
        assertEquals("John", result.getFirstName());
        verify(doctorRepository, times(1)).findDoctorByMcrNoAndPassword("M12345A", "password123");
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNoAndPassword("M12345A", "wrongpassword"))
            .thenReturn(null);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            doctorService.login("M12345A", "wrongpassword");
        });
        verify(doctorRepository, times(1)).findDoctorByMcrNoAndPassword("M12345A", "wrongpassword");
    }

    @Test
    void testFindDoctorByMcrNo_Success() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(testDoctor);

        // Act
        Doctor result = doctorService.findDoctorByMcrNo("M12345A");

        // Assert
        assertNotNull(result);
        assertEquals("M12345A", result.getMcrNo());
        assertEquals("John", result.getFirstName());
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
    }

    @Test
    void testFindDoctorByMcrNo_NotFound() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("INVALID")).thenReturn(null);

        // Act
        Doctor result = doctorService.findDoctorByMcrNo("INVALID");

        // Assert
        assertNull(result);
        verify(doctorRepository, times(1)).findDoctorByMcrNo("INVALID");
    }

    @Test
    void testRegisterDoctor_Success() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(null);
        when(clinicRepository.findClinicByClinicName("Test Clinic")).thenReturn(testClinic);
        when(doctorRepository.saveAndFlush(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        Doctor result = doctorService.registerDoctor(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals("M12345A", result.getMcrNo());
        assertEquals("John", result.getFirstName());
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(clinicRepository, times(1)).findClinicByClinicName("Test Clinic");
        verify(doctorRepository, times(1)).saveAndFlush(any(Doctor.class));
    }

    @Test
    void testRegisterDoctor_AlreadyExists() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(testDoctor);

        // Act & Assert
        assertThrows(UserAlreadyExist.class, () -> {
            doctorService.registerDoctor(registrationRequest);
        });
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(doctorRepository, never()).saveAndFlush(any(Doctor.class));
    }

    @Test
    void testRegisterDoctor_ClinicNotFound() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(null);
        when(clinicRepository.findClinicByClinicName("NonExistent Clinic")).thenReturn(null);

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            registrationRequest.setClinicName("NonExistent Clinic");
            doctorService.registerDoctor(registrationRequest);
        });
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(clinicRepository, times(1)).findClinicByClinicName("NonExistent Clinic");
        verify(doctorRepository, never()).saveAndFlush(any(Doctor.class));
    }

    @Test
    void testUpdateDoctor_Success() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(testDoctor);
        when(doctorRepository.saveAndFlush(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        Doctor result = doctorService.updateDoctor(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("M12345A", result.getMcrNo());
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(doctorRepository, times(1)).saveAndFlush(any(Doctor.class));
    }

    @Test
    void testUpdateDoctor_NotFound() {
        // Arrange
        when(doctorRepository.findDoctorByMcrNo("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ItemNotFound.class, () -> {
            updateRequest.setMcrNo("INVALID");
            doctorService.updateDoctor(updateRequest);
        });
        verify(doctorRepository, times(1)).findDoctorByMcrNo("INVALID");
        verify(doctorRepository, never()).saveAndFlush(any(Doctor.class));
    }

    @Test
    void testUpdateDoctor_WithPassword() {
        // Arrange
        updateRequest.setPassword("newpassword123");
        when(doctorRepository.findDoctorByMcrNo("M12345A")).thenReturn(testDoctor);
        when(doctorRepository.saveAndFlush(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        Doctor result = doctorService.updateDoctor(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("M12345A", result.getMcrNo());
        verify(doctorRepository, times(1)).findDoctorByMcrNo("M12345A");
        verify(doctorRepository, times(1)).saveAndFlush(any(Doctor.class));
    }
}
