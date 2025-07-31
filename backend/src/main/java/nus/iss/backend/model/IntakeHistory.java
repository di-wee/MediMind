package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "Medication Intake History")
public class IntakeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private int id;
    @Column(name = "Logged_Date")
    private Date loggedDate;
    @Column(name="IsTaken",nullable = false)
    private boolean isTaken;
    @Column(name = "DoctorNote")
    private String doctorNote;
    @ManyToOne
    @JoinColumn(name = "Patient_id",nullable = false)
    private Patient patient;
    @ManyToOne
    @JoinColumn(name="Medication_schedule_id",nullable = false)
    private Schedule schedule;
}
