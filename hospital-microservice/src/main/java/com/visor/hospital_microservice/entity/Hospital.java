package com.visor.hospital_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String idKeycloak;

    @Size(max = 100, message = "Hospital name must be 100 characters or fewer")
    @Schema(example = "Hospital Central")
    private String name;

    @Size(max = 100, message = "Address must be 100 characters or fewer")
    @Schema(example = "Mendoza")
    private String address;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid and include country code")
    @Schema(example = "+54912345678")
    private String phoneNumber;

    @Email(message = "Email must be a valid format")
    @Schema(example = "diegombustos16@gmail.com")
    private String email;

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
