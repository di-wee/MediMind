package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "Medication Intake History")
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Clinic_UUID")
    private String id;
    @Column(name = "Clinic_name")
    private String Clinic_name;
}
