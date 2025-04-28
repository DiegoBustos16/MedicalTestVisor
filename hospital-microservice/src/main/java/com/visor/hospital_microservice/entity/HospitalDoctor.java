package com.visor.hospital_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDoctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @NotNull(message = "hospitalId is required")
    @Column(nullable = false)
    @Schema(example = "1")
    private Long hospitalId;

    @NotNull(message = "doctorId is required")
    @Column(nullable = false)
    @Schema(example = "1")
    private Long doctorId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
