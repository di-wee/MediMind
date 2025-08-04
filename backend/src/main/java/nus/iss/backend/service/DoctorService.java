package nus.iss.backend.service;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.RegistrationRequestWeb;
import nus.iss.backend.model.Doctor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface DoctorService {

    Doctor login(String mcrNo, String password);

    Doctor findDoctorByMcrNo(String mcrNo);

    Doctor registerDoctor(RegistrationRequestWeb request);

    Doctor updateDoctor(DoctorUpdateReqWeb request);
}
