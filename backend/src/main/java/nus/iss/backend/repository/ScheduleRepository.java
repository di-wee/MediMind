package nus.iss.backend.repository;

import nus.iss.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule,String> {
    List<Schedule> findSchedulesByScheduledTime(LocalDateTime scheduledTime);
}
