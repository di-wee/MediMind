package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private int id;
    @Column(name = "Scheduled_time")
    private Date scheduled_time;
    @ManyToOne
    @JoinColumn(name = "Medication_id",nullable = false)
    private Medication medication;
    @ManyToOne
    @JoinColumn(name="Patient_id",nullable = false)
    private Patient patient;
    @OneToMany(mappedBy = "schedule")
    private List<IntakeHistory> intakeHistory;
}
