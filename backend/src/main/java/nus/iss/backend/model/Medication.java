package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Medication")
public class Medication{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "Medication_Name",nullable = false)
    private String medicationName;
    @Column(name="Dosage",nullable = false)
    private String dosage;
    @Column(name = "Intake_Quantity",nullable = false)
    private String intakeQuantity;
    @Column(name = "Frequency",nullable = false)
    private String frequency;
    @Column(name = "Timing")
    private String timing;
    @Column(name = "Instructions", nullable = false)
    private String instructions;
    @Column(name = "Doctor_Notes")
    private String doctorNotes;
    @Column(name = "Is_Active", nullable = false)
    private boolean isActive = true;

    @ManyToMany(mappedBy = "medications")
    private List<Patient> patients;
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> schedules;
}