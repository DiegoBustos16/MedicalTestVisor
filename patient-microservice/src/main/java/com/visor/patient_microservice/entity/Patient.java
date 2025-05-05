package com.visor.patient_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "Identification number must not be null")
    private String identificationNumber;

    @Schema(example = "Diego")
    @NotNull(message = "First name must not be null")
    private String firstName;

    @Schema(example = "Bustos")
    @NotNull(message = "Last name must not be null")
    private String lastName;

    @Schema(example = "16/02/2000")
    @NotNull(message = "Birth date must not be null")
    private LocalDate dateOfBirth;

    @Schema(example = "Male")
    @NotNull(message = "Gender must not be null")
    private String gender;

    @Schema(example = "diegombustos16@gmail.com")
    @Email(message = "Email must be a valid format")
    @NotNull(message = "Email must not be null")
    private String email;

    @Schema(example = "+54912345678")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid and include country code")
    @NotNull(message = "Phone number must not be null")
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
