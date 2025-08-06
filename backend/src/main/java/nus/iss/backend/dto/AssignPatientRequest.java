package nus.iss.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class AssignPatientRequest {
    private UUID patientId;
    private String doctorId;
}