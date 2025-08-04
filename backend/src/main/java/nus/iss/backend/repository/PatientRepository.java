package nus.iss.backend.repository;

import nus.iss.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * This is an explicit finder. Kept if the team is using it elsewhere.
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
}
