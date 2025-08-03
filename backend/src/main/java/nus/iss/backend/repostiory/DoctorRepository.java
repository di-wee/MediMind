package nus.iss.backend.repostiory;

import nus.iss.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    Doctor findDoctorByMcrNoAndPassword(String mcrNo, String password);

    Doctor findDoctorByMcrNo(String mcrNo);
}
