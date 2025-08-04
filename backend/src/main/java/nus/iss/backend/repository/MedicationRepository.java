package nus.iss.backend.repository;

import nus.iss.backend.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface MedicationRepository extends JpaRepository<Medication,UUID> {
    Medication findMedicationById(UUID id);
    List<Medication> findAllByIdIn(List<UUID> medIds);
}
