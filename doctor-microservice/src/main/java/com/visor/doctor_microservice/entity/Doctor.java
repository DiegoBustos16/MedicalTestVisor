package com.visor.doctor_microservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String idKeycloak;

    @Schema(example = "ABC123")
    @Column(unique = true)
    private String licenseNumber;

    @Schema(example = "Diego")
    private String firstName;

    @Schema(example = "Bustos")
    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(example = "16/02/2000")
    private LocalDate dateOfBirth;

    @Schema(example = "Male")
    private String gender;

    @Schema(example = "diegombustos16@gmail.com")
    @Column(unique = true,nullable = false)
    private String email;

    @Schema(example = "+54912345678")
    @Column(unique = true)
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
