package com.visor.hospital_microservice.repository;

import com.visor.hospital_microservice.entity.HospitalDoctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalDoctorRepository extends JpaRepository<HospitalDoctor, Long> {
    boolean existsByIdAndDeletedAtIsNull(Long id);

    Optional<HospitalDoctor> findByDoctorIdAndHospitalIdAndDeletedAtIsNull (Long doctorId, Long hospitalId);

    Optional<HospitalDoctor> findByDoctorIdAndDeletedAtIsNull (Long doctorId);

    Optional<HospitalDoctor> findByHospitalIdAndDeletedAtIsNull (Long hospitalId);
}
