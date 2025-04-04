package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    public Hospital createHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Optional<Hospital> getHospitalById(Long id) {
        Optional<Hospital> hospital = hospitalRepository.findById(id);
        return hospital;
    }
    public Long findIdbyKeycloak(String keycloakId) {
        Optional<Hospital> hospital = hospitalRepository.findByIdKeycloakAndDeletedAtIsNull(keycloakId);
        if (hospital.isPresent()) {
            return hospital.get().getId();
        }
        return null;
    }

    public Hospital updateHospital(Long id, Hospital hospital) {
        if (hospitalRepository.existsByIdAndDeletedAtIsNull(id)) {
            hospital.setId(id);
            return hospitalRepository.save(hospital);
        }
        return null;
    }

    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id).orElse(null);
        if (hospital != null) {
            hospital.setDeletedAt(Instant.now());
            hospitalRepository.save(hospital);
        }
    }

}
