package nus.iss.backend.service;

import nus.iss.backend.model.Clinic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface ClinicService {

    List<Clinic> getAllClinics();
    Clinic findByClinicName(String clinicName);
}
