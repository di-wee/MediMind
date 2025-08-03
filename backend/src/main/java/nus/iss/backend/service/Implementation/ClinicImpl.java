package nus.iss.backend.service.Implementation;

import nus.iss.backend.model.Clinic;
import nus.iss.backend.repostiory.ClinicRepository;
import nus.iss.backend.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClinicImpl implements ClinicService {

    @Autowired
    private ClinicRepository clinicRepo;

    @Override
    public List<Clinic> getAllClinics() {
        return clinicRepo.findAll();
    }

    @Override
    public Clinic findClinicByClinicName(String clinicName) {
        return clinicRepo.findClinicByClinicName(clinicName);
    }




}
