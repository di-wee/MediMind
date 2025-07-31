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
@Table(name = "Schedule")
public class Schedule {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "Scheduled_Time")
    private Date scheduledTime;
    @ManyToOne
    @JoinColumn(name = "Medication_Id",nullable = false)
    private Medication medication;
    @ManyToOne
    @JoinColumn(name="Patient_Id",nullable = false)
    private Patient patient;
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IntakeHistory> intakeHistory;
}
