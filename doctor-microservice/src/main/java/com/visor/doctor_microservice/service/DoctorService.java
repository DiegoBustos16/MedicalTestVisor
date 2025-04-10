package com.visor.doctor_microservice.service;


import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.exception.DuplicateResourceException;
import com.visor.doctor_microservice.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.visor.doctor_microservice.exception.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor createDoctor(Doctor doctor) {
        try {
            return doctorRepository.save(doctor);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A doctor with this keycloakId already exists.");
        }
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAllByDeletedAtIsNull();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                new ResourceNotFoundException("No active doctor found with ID: " + id));
    }

    public Doctor getDoctorByLicenseNumber(String licenseNumber) {
        return doctorRepository.findByLicenseNumberAndDeletedAtIsNull(licenseNumber)
                .orElseThrow(() ->
                new ResourceNotFoundException("No active doctor found with license number: " + licenseNumber));
    }

    public Long getDoctorIdByKeycloakId(String keycloakId) {
        return doctorRepository.findByIdKeycloakAndDeletedAtIsNull(keycloakId)
                .map(Doctor::getId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active doctor found with Keycloak ID: " + keycloakId));

    }

    public Doctor patchDoctor(Long id, Doctor partialDoctor) {
        Doctor existing = doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active doctor found with ID: " + id));

        BeanUtils.copyProperties(partialDoctor, existing, getNullOrRestrictedProperties(partialDoctor));
        return doctorRepository.save(existing);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cannot delete. No active doctor found with ID: " + id));

        doctor.setDeletedAt(Instant.now());
        doctorRepository.save(doctor);
    }

    private String[] getNullOrRestrictedProperties(Object source) {
        final BeanWrapper wrapper = new BeanWrapperImpl(source);

        List<String> nullOrRestricted = new ArrayList<>();

        for (var pd : wrapper.getPropertyDescriptors()) {
            String propertyName = pd.getName();
            Object value = wrapper.getPropertyValue(propertyName);

            if (value == null || List.of("id", "idKeycloak", "createdAt", "deletedAt").contains(propertyName)) {
                nullOrRestricted.add(propertyName);
            }
        }

        return nullOrRestricted.toArray(new String[0]);
    }
}
