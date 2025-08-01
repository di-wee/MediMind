package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequestWeb {
    private String firstName, lastName, mcrNo, email, clinicName, password;
}
