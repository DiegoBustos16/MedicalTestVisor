package com.visor.hospital_microservice.repository;

import com.visor.hospital_microservice.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    boolean existsByIdKeycloakAndDeletedAtIsNull(String id);

    boolean existsByIdKeycloakAndDeletedAtIsNotNull(String id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    Optional<Hospital> findByIdKeycloakAndDeletedAtIsNull(String id);
}
