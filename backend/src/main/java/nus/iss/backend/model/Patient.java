package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Patient")
public class Patient {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
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
    private LocalDate dob;

    @ManyToOne
    @JoinColumn(name = "Assigned_Doctor")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "Clinic_UUID", nullable = false)
    private Clinic clinic;

    @ManyToMany
    @JoinTable(
            name = "Patient_Medication",
            joinColumns = @JoinColumn(name = "Patient_Id"),
            inverseJoinColumns = @JoinColumn(name = "Medication_Id")
    )
    private List<Medication> medications = new ArrayList<>();
}
