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
public class HospitalDoctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @Schema(example = "1")
    private Long hospitalId;

    @Schema(example = "1")
    private Long doctorId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
