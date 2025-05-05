package com.visor.patient_microservice.repository;
import com.visor.patient_microservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findById(Long id);
    boolean existsPatientById(Long id);
    Optional<Patient> findByIdAndDeletedAtIsNull(Long id);
}
