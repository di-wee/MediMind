package nus.iss.backend.repository;

import nus.iss.backend.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface MedicationRepository extends JpaRepository<Medication,UUID> {
    Medication findMedicationById(UUID id);
    List<Medication> findAllByIdIn(List<UUID> medIds);

    @Query("SELECT m FROM Medication m JOIN m.patients p WHERE p.id = :patientId AND LOWER(m.medicationName) = LOWER(:medicationName) AND m.isActive = true")
    List<Medication> findByPatientIdAndMedicationNameIgnoreCase(@Param("patientId") UUID patientId, @Param("medicationName") String medicationName);
}
