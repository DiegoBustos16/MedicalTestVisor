package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class HospitalDoctorService {
    @Autowired
    private HospitalDoctorRepository hospitalDoctorRepository;

    public HospitalDoctor createHospitalDoctor(HospitalDoctor hospitalDoctor) {
        return hospitalDoctorRepository.save(hospitalDoctor);
    }

    public Optional<HospitalDoctor> getHospitalDoctorById(Long id) {
        Optional<HospitalDoctor> hospitalDoctor = hospitalDoctorRepository.findById(id);
        return hospitalDoctor;
    }

    public Optional<HospitalDoctor> getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(Long doctorId, Long hospitalId) {
        return hospitalDoctorRepository.findByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);
    }

    public Optional<HospitalDoctor> getHospitalDoctorByDoctorIdAndDeletedAtIsNull(Long doctorId) {
        return hospitalDoctorRepository.findByDoctorIdAndDeletedAtIsNull(doctorId);
    }

    public Optional<HospitalDoctor> getHospitalDoctorByHospitalIdAndDeletedAtIsNull(Long hospitalId) {
        return hospitalDoctorRepository.findByHospitalIdAndDeletedAtIsNull(hospitalId);
    }

    public HospitalDoctor updateHospitalDoctor(Long id, HospitalDoctor hospitalDoctor) {
        if (hospitalDoctorRepository.existsByIdAndDeletedAtIsNull(id)) {
            hospitalDoctor.setId(id);
            return hospitalDoctorRepository.save(hospitalDoctor);
        }
        return null;
    }

    public void deleteHospitalDoctor(Long id) {
        Optional<HospitalDoctor> hospitalDoctor = hospitalDoctorRepository.findById(id);
        hospitalDoctor.ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            hospitalDoctorRepository.save(entity);
        });
    }
}
