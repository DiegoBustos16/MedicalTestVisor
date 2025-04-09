package com.visor.patient_microservice.service;

import com.visor.patient_microservice.entity.Patient;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import com.visor.patient_microservice.repository.PatientRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public boolean existPatientById(Long id) {
        return patientRepository.existsPatientById(id);
    }
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }
    public List<Patient> searchPatients(Patient filter) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Patient> example = Example.of(filter, matcher);
        return patientRepository.findAll(example);
    }
    public Patient updatePatient(Long id, Patient patient) {
        if (patientRepository.existsById(id)) {
            patient.setId(id);
            return patientRepository.save(patient);
        }
        return null;
    }

    public void deletePatient(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        patient.ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            patientRepository.save(entity);
        });
    }
}
