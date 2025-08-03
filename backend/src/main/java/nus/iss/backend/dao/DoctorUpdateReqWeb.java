package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;
import nus.iss.backend.model.Clinic;

import java.util.UUID;

@Getter
@Setter
public class DoctorUpdateReqWeb {
    private String mcrNo, email, password;
    private Clinic clinic;
}
