package nus.iss.backend.repostiory;

import nus.iss.backend.model.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, UUID> {
    Clinic findClinicByClinicName(String clinicName);

    Clinic findClinicById(UUID id);
}
