package nus.iss.backend.repostiory;

import nus.iss.backend.model.IntakeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IntakeHistoryRepository extends JpaRepository<IntakeHistory, UUID> {
}
