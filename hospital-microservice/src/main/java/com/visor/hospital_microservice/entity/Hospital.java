package com.visor.hospital_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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

    @Column(unique = true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String idKeycloak;

    @Schema(example = "Hospital Central")
    private String name;

    @Schema(example = "Mendoza")
    private String address;

    @Schema(example = "+54912345678")
    private String phoneNumber;

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
