package com.visor.patient_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true)
    @Schema(example = "42563459")
    private String identificationNumber;

    @Schema(example = "Diego")
    private String firstName;

    @Schema(example = "Bustos")
    private String lastName;

    @Schema(example = "16/02/2000")
    private LocalDate dateOfBirth;

    @Schema(example = "Male")
    private String gender;

    @Schema(example = "diegombustos16@gmail.com")
    private String email;

    @Schema(example = "+54912345678")
    private String phoneNumber;

    @Column(nullable = false, updatable = false)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
