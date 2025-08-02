package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalTime;
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
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "Scheduled_Time")
    private LocalTime scheduledTime;

    @Column(name = "Is_Active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "Medication_Id",nullable = false)
    private Medication medication;
    @ManyToOne
    @JoinColumn(name="Patient_Id",nullable = false)
    private Patient patient;
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IntakeHistory> intakeHistory;
}
