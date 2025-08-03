package nus.iss.backend.service;

import nus.iss.backend.model.Clinic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public interface ClinicService {

    List<Clinic> getAllClinics();


    Clinic findClinicByClinicName(String clinicName);
}
