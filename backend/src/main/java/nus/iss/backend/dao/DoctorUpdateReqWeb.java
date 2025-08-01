package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorUpdateReqWeb {
    private String McrNo, email,clinicName, password;
}
