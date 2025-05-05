package com.visor.patient_microservice.service;

import com.visor.patient_microservice.entity.Patient;
import com.visor.patient_microservice.exception.BadRequestException;
import com.visor.patient_microservice.exception.DuplicateResourceException;
import com.visor.patient_microservice.exception.ResourceNotFoundException;
import com.visor.patient_microservice.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        try {
            return patientRepository.save(patient);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A patients with the same identification number already exists.");
        }
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active patient found with ID: " + id));

    }

    public boolean existPatientById(Long id) {
        return patientRepository.existsPatientById(id);
    }

    public List<Patient> searchPatients(Patient filter) {
        if (filter == null || isFilterEmpty(filter)) {
            throw new BadRequestException("Filter cannot be null or empty");
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Patient> example = Example.of(filter, matcher);
        return patientRepository.findAll(example);
    }

    public Patient updatePatient(Long id, Patient patient) {
        Patient existing = patientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active Patient found with ID: " + id));

        patient.setId(existing.getId());
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active Patient found with ID: " + id));

        patient.setDeletedAt(Instant.now());
        patientRepository.save(patient);
    }

    private boolean isFilterEmpty(Patient filter) {
        return isNullOrEmpty(filter.getFirstName()) &&
               isNullOrEmpty(filter.getLastName()) &&
               isNullOrEmpty(filter.getIdentificationNumber()) &&
               isNullOrEmpty(filter.getEmail()) &&
               isNullOrEmpty(filter.getPhoneNumber());
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}
