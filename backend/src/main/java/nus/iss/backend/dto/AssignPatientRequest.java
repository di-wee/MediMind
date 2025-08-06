package nus.iss.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignPatientRequest {
    private String patientId;
    private String doctorId;
}