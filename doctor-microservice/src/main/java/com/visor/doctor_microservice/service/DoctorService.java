package com.visor.doctor_microservice.service;


import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        System.out.println("Buscando doctor con id " + id + ": " + doctor);
        return doctor;
        //return doctorRepository.findById(id);
    }

    public Optional<Doctor> getDoctorByLicenseNumber(String licenseNumber) {
        return doctorRepository.findByLicenseNumber(licenseNumber);
    }

    public Long findIdbyKeycloak(String keycloakId) {
        Optional<Doctor> doctor = doctorRepository.findByIdKeycloakAndDeletedAtIsNull(keycloakId);
        if (doctor.isPresent()) {
            return doctor.get().getId();
        }
        return null;
    }

    public Doctor updateDoctor(Long id, Doctor doctor) {
        if (doctorRepository.existsByIdAndDeletedAtIsNull(id)) {
            doctor.setId(id);
            return doctorRepository.save(doctor);
        }
        return null;
    }

    public void deleteDoctor(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        doctor.ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            doctorRepository.save(entity);
        });
    }
}
