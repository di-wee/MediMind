package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Doctor")
public class Doctor {
    @Id
    @Column(name = "MCR_no")
    private String mcr_no;
    @Column(name="Password",nullable = false)
    private String password;
    @Column(name = "FirstName",nullable = false)
    private String firstName;
    @Column(name = "LastName", nullable = false)
    private String lastName;
    @Column(name = "Email", nullable = false)
    private String email;
    @OneToOne
    @JoinColumn(name = "Practicing_clinic_UUID")
    private Clinic clinic;
    @OneToMany(mappedBy = "doctor")
    private List<Patient> patients;
}

