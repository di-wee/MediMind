package nus.iss.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "MCR No is required.")
    @Size(min = 7, max = 7, message = "MCR No must be 7 characters long.")
    private String mcrNo;

    @Column(name = "Password", nullable = false)
    @NotBlank(message = "Password is required.")
    private String password;

    @Column(name = "FirstName", nullable = false)
    @NotBlank(message = "First name is required.")
    private String firstName;

    @Column(name = "LastName", nullable = false)
    @NotBlank(message = "Last name is required.")
    private String lastName;

    @Column(name = "Email", nullable = false)
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format!")
    private String email;


    @ManyToOne
    @JoinColumn(name = "Clinic_UUID", nullable = false)
    private Clinic clinic;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients;

}



