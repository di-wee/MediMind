package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.RegistrationRequestWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.exceptions.UserAlreadyExist;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.repository.ClinicRepository;
import nus.iss.backend.repository.DoctorRepository;
import nus.iss.backend.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorImpl implements DoctorService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorImpl.class);

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private ClinicRepository clinicRepository;

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

    @Override
    public Doctor registerDoctor(RegistrationRequestWeb request) {
        Doctor doctor = doctorRepo.findDoctorByMcrNo(request.getMcrNo());

        if (doctor != null) {
            throw new UserAlreadyExist("Doctor is already registered!");
        }

        Clinic clinic = clinicRepository.findClinicByClinicName(request.getClinicName());

        if (clinic == null) {
            logger.error("Clinic name: " + request.getClinicName());
            throw new ItemNotFound("Clinic not found!");
        }
        Doctor newDoctor = new Doctor();
        newDoctor.setMcrNo(request.getMcrNo());
        newDoctor.setFirstName(request.getFirstName());
        newDoctor.setLastName(request.getLastName());
        newDoctor.setEmail(request.getEmail());
        newDoctor.setPassword(request.getPassword());
        newDoctor.setClinic(clinic);

        doctorRepo.saveAndFlush(newDoctor);

        return newDoctor;

    }
    public Doctor updateDoctor(DoctorUpdateReqWeb request) {
        Doctor doctor = doctorRepo.findDoctorByMcrNo(request.getMcrNo());
        if (doctor == null) {
            throw new ItemNotFound("Doctor not found!");
        }
        if(request.getClinic() != null){
            Clinic clinic = request.getClinic();
            if (clinic == null) {
                logger.error("Clinic uuid: " + request.getClinic());
                throw new ItemNotFound("Clinic not found!");
            }
            doctor.setClinic(clinic);
        }
        if (request.getPassword() != null) {
            doctor.setPassword(request.getPassword());
        }
        if (request.getEmail() != null) {
            doctor.setEmail(request.getEmail());
        }
        doctorRepo.saveAndFlush(doctor);
        return doctor;
    }

}
