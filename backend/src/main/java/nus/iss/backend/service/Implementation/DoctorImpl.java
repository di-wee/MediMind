package nus.iss.backend.service.Implementation;

import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.repostiory.DoctorRepository;
import nus.iss.backend.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;

@Service
@Transactional
public class DoctorImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    public Doctor login (String mcrNo, String password) {
        Doctor doctor = doctorRepo.findDoctorByMcrNoAndPassword(mcrNo, password);
        if (doctor == null) {
            throw new InvalidCredentialsException("Invalid Credentials!");
        }
        return doctor;
    }

    public Doctor findDoctorByMcrNo (String mcrNo) {
        return doctorRepo.findDoctorByMcrNo(mcrNo);

    }

}
