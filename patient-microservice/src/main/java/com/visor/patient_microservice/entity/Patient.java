package com.visor.patient_microservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String identificationNumber;


    private String firstName;


    private String lastName;


    private LocalDate dateOfBirth;


    private String gender;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
