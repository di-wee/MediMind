package nus.iss.backend.service;

import nus.iss.backend.model.Doctor;
import nus.iss.backend.repostiory.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface DoctorService {

    Doctor login(String mcrNo, String password);

    Doctor findDoctorByMcrNo(String mcrNo);


}
