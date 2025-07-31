package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private int id;
    @Column(name = "Email")
    private String email;
    @Column(name="Password",nullable = false)
    private String password;
    @Column(name = "NRIC",nullable = false)
    private String nric;
    @Column(name = "FirstName",nullable = false)
    private String firstName;
    @Column(name = "LastName", nullable = false)
    private String lastName;
    @Column(name = "Gender", nullable = false)
    private String gender;
    @Column(name = "DOB", nullable = false)
    private Date dob;
    @Column(name = "ClinicName", nullable = false)
    private String clinicName;
    @ManyToOne
    @JoinColumn(name = "AssignedDoctor")
    private Doctor doctor;
    @ManyToMany
    private List<Medication> medications;
}
