package com.visor.doctor_microservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String idKeycloak;

    @Size(max = 50, message = "License number must be 50 characters or fewer")
    @Schema(example = "ABC123")
    @Column(unique = true)
    private String licenseNumber;

    @Size(max = 100, message = "First name must be 100 characters or fewer")
    @Schema(example = "Diego")
    private String firstName;

    @Size(max = 100, message = "Last name must be 100 characters or fewer")
    @Schema(example = "Bustos")
    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(example = "16/02/2000")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female, or Other")
    @Schema(example = "Male")
    private String gender;

    @Email(message = "Email must be a valid format")
    @Schema(example = "diegombustos16@gmail.com")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid and include country code")
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
