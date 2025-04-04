package com.visor.hospital_microservice.entity;

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
    private Long id;

    private Instant createdAt;
    private Instant deletedAt;

    private Long hospitalId;
    private Long doctorId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
