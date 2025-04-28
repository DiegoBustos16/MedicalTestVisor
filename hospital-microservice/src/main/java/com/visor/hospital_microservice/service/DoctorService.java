package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private final DoctorClient doctorClient;
    @Autowired
    private final HospitalDoctorRepository hospitalDoctorRepository;

    public DoctorService(DoctorClient doctorClient, HospitalDoctorRepository hospitalDoctorRepository) {
        this.doctorClient = doctorClient;
        this.hospitalDoctorRepository = hospitalDoctorRepository;
    }

    public List<DoctorDTO> getAllDoctorsByHospital(Long hospitalId) {

        List<HospitalDoctor> relations = hospitalDoctorRepository
                .findAllByHospitalIdAndDeletedAtIsNull(hospitalId);

        return relations.stream()
                .map(relation -> doctorClient.getDoctorById(relation.getDoctorId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("No active doctor found with ID: " + relation.getDoctorId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

