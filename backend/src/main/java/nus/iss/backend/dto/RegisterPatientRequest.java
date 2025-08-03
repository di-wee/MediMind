package nus.iss.backend.dto;

import java.util.UUID;

public class RegisterPatientRequest {
    public String email;
    public String password;
    public String nric;
    public String firstName;
    public String lastName;
    public String gender;
    public String dob;    // Expecting YYYY-MM-DD
    public UUID clinicId;   // optional
    public String clinicName; // optional
}
