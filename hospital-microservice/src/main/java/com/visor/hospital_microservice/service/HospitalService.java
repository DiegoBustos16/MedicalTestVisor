package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.exception.DuplicateResourceException;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    public Hospital createHospital(Hospital hospital) {
        try {
            return hospitalRepository.save(hospital);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A hospital with this keycloakId already exists.");
        }
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active hospital found with ID: " + id));
    }

    public Long getHospitalIdByKeycloakId(String keycloakId) {
        return hospitalRepository.findByIdKeycloakAndDeletedAtIsNull(keycloakId)
                .map(Hospital::getId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active hospital found with Keycloak ID: " + keycloakId));

    }

    public Hospital patchHospital(Long id, Hospital partialHospital) {
        Hospital existing = hospitalRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active hospital found with ID: " + id));

        BeanUtils.copyProperties(partialHospital, existing, getNullOrRestrictedProperties(partialHospital));
        return hospitalRepository.save(existing);
    }

    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cannot delete. No active hospital found with ID: " + id));

        hospital.setDeletedAt(Instant.now());
        hospitalRepository.save(hospital);
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
