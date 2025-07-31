package nus.iss.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Medication_Intake_History")
public class IntakeHistory {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name="Id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "Logged_Date")
    private Date loggedDate;
    @Column(name="Is_Taken",nullable = false)
    private boolean isTaken;
    @Column(name = "Doctor_Note")
    private String doctorNote;

    @ManyToOne
    @JoinColumn(name = "Patient_Id",nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name="Schedule_Id",nullable = false)
    private Schedule schedule;
}
