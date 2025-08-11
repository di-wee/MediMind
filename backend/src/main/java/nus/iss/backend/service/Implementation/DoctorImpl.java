package nus.iss.backend.service.Implementation;

import nus.iss.backend.dao.DoctorUpdateReqWeb;
import nus.iss.backend.dao.RegistrationRequestWeb;
import nus.iss.backend.exceptions.InvalidCredentialsException;
import nus.iss.backend.exceptions.InvalidEmailDomainException;
import nus.iss.backend.exceptions.ItemNotFound;
import nus.iss.backend.exceptions.UserAlreadyExist;
import nus.iss.backend.model.Clinic;
import nus.iss.backend.model.Doctor;
import nus.iss.backend.repository.ClinicRepository;
import nus.iss.backend.repository.DoctorRepository;
import nus.iss.backend.service.DoctorService;
import nus.iss.backend.service.PatientService;
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
    @Autowired
    private PatientService patientService;

    public Doctor login (String mcrNo, String password) {
        Doctor doctor = doctorRepo.findDoctorByMcrNoAndPassword(mcrNo, password);
        if (doctor == null) {
            throw new InvalidCredentialsException("Invalid Credentials!");
        }
        return doctor;
    }

    private void validateEmailDomain(String email, Clinic clinic) {
        if (clinic.getEmailDomain() == null || clinic.getEmailDomain().trim().isEmpty()) {
            logger.warn("No email domain configured for clinic: {}", clinic.getClinicName());
            return; // Skip validation if no domain is configured
        }

        if (!email.contains("@")) {
            throw new InvalidEmailDomainException("Invalid email format: missing @ symbol");
        }

        String emailDomain = email.substring(email.indexOf("@") + 1).toLowerCase();
        String clinicDomain = clinic.getEmailDomain().toLowerCase();

        if (!emailDomain.equals(clinicDomain)) {
            logger.error("Email domain mismatch. Expected: {}, Got: {}", clinicDomain, emailDomain);
            throw new InvalidEmailDomainException(
                    String.format("Email domain '%s' does not match clinic domain '%s'. " +
                                    "Please use an email address from your clinic's domain.",
                            emailDomain, clinicDomain)
            );
        }

        logger.info("Email domain validation passed for clinic: {}", clinic.getClinicName());
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
        // validate email domain
        validateEmailDomain(request.getEmail(), clinic);

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
        Clinic targetClinic = doctor.getClinic(); // default is current clinic
        boolean clinicChanged = false;

        // update clinic logic
        if(request.getClinic() != null){
            Clinic newClinic = request.getClinic();
            // check if the clinic is different from the current one
            if (!newClinic.getId().equals(doctor.getClinic().getId())) {
                logger.info("Doctor {} changing clinic from {} to {}",
                        request.getMcrNo(), doctor.getClinic().getClinicName(), newClinic.getClinicName());

                patientService.unassignAllPatientsFromDoctor(doctor.getMcrNo());
                doctor.setClinic(newClinic);
                targetClinic = newClinic; // update target clinic to new clinic
                clinicChanged = true; // mark that the clinic has changed
            }

        }

        //update password logic
        if (request.getPassword() != null) {
            doctor.setPassword(request.getPassword());
        }

        //update email logic
        if (request.getEmail() != null) {
            String newEmail = request.getEmail();

            validateEmailDomain(newEmail, targetClinic);
            doctor.setEmail(newEmail);
            logger.info("Doctor {} email updated to: {}", request.getMcrNo(), newEmail);

        } else if (clinicChanged) {
            validateEmailDomain(doctor.getEmail(), targetClinic);
            logger.info("Doctor {} clinic changed, current email validated against new clinic: {}",
                    request.getMcrNo(), targetClinic.getClinicName());
        }
        doctorRepo.saveAndFlush(doctor);
        return doctor;
    }




}
