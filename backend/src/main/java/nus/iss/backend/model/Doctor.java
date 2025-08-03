package nus.iss.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Doctor")
public class Doctor {
    @Id
    @Column(name = "MCR_No")
    private String mcrNo;
    @Column(name="Password",nullable = false)
    private String password;
    @Column(name = "FirstName",nullable = false)
    private String firstName;
    @Column(name = "LastName", nullable = false)
    private String lastName;
    @Column(name = "Email", nullable = false)
    private String email;


    @ManyToOne
    @JoinColumn(name = "Clinic_UUID", nullable = false)
    private Clinic clinic;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients;

//    @JsonProperty("patientIds")
//    public List<UUID> getPatientIds() {
//        return patients == null ? List.of() :
//                patients.stream().map(Patient::getId).toList();
//    }
}

