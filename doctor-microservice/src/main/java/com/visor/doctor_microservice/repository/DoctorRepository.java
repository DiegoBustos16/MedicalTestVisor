package com.visor.doctor_microservice.repository;

import com.visor.doctor_microservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByLicenseNumberAndDeletedAtIsNull(String licenseNumber);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    boolean existsByIdKeycloakAndDeletedAtIsNull(String id);

    boolean existsByIdKeycloakAndDeletedAtIsNotNull(String id);

    Optional<Doctor> findById(Long id);

    Optional<Doctor> findByIdAndDeletedAtIsNull(Long id);

    List <Doctor> findAllByDeletedAtIsNull();

    Optional<Doctor> findByIdKeycloakAndDeletedAtIsNull(String id);
}
