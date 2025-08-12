package nus.iss.backend.repository;

import nus.iss.backend.model.Doctor;
import nus.iss.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * This is an explicit finder.
     */
    Patient findPatientById(UUID id);

    /**
     * Custom finder to locate a patient by email and password.
     * Used by the login endpoint.
     *
     * Spring Data JPA will automatically generate the query:
     * SELECT * FROM patient WHERE email = ? AND password = ?
     */
    Optional<Patient> findByEmailAndPassword(String email, String password);

    List<Patient> findByDoctorMcrNo(String mcr);

    List<Patient> findByClinic_IdAndDoctorIsNull(UUID clinicId);

    List<Patient> findByDoctor(Doctor doctor);

    /**
     * Returns true if any Patient exists with the given email (case-insensitive).
     */
    @Query("select (count(p) > 0) from Patient p where lower(p.email) = lower(:email)")
    boolean existsEmailIgnoreCase(@Param("email") String email);

    /**
     * Returns true if any Patient other than the given id has the given email (case-insensitive).
     * Useful for profile updates to exclude the current patient record.
     */
    @Query("""
           select (count(p) > 0)
           from Patient p
           where lower(p.email) = lower(:email)
             and p.id <> :excludeId
           """)
    boolean existsEmailIgnoreCaseExcludingId(@Param("email") String email,
                                             @Param("excludeId") UUID excludeId);
}
