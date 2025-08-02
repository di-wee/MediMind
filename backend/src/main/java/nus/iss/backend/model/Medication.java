package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.ArrayList;
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
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "Medication_Name",nullable = false)
    private String medicationName;
    @Column(name = "Intake_Quantity",nullable = false)
    private String intakeQuantity;
    @Column(name = "Frequency",nullable = false)
    private int frequency;
    @Column(name = "Timing")
    private String timing;
    @Column(name = "Instructions")
    private String instructions;
    @Column(name = "Notes")
    private String notes;
    @Column(name = "Is_Active", nullable = false)
    private boolean isActive = true;


    @ManyToMany(mappedBy = "medications")
    private List<Patient> patients = new ArrayList<>();
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> schedules;
}