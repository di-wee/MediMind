package nus.iss.backend.repository;

import nus.iss.backend.model.IntakeHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface IntakeHistoryRepository extends CrudRepository<IntakeHistory, UUID> {

    /**
     * Fetch all intake history entries for a given patient,
     * along with their schedule and medication information.
     */
    @Query("SELECT i FROM IntakeHistory i " +
           "JOIN FETCH i.schedule s " +
           "JOIN FETCH s.medication m " +
           "WHERE i.patient.id = :patientId")
    List<IntakeHistory> findByPatientId(UUID patientId);
}
