package nus.iss.backend.repository;

import nus.iss.backend.model.IntakeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntakeRepository extends JpaRepository<IntakeHistory, UUID> {
    List<IntakeHistory> findBySchedule_Medication_Id(UUID scheduleMedicationId);
}
