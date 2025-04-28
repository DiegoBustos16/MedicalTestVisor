package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.exception.ForbiddenOperationException;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class HospitalDoctorService {
    @Autowired
    private HospitalDoctorRepository hospitalDoctorRepository;
    @Autowired
    private DoctorClient doctorClient;

    public HospitalDoctor createHospitalDoctor(Long doctorId, Long idHospital) {
        doctorClient.getDoctorById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active Doctor found with ID: " + doctorId));

        HospitalDoctor hospitalDoctor = new HospitalDoctor();
        hospitalDoctor.setHospitalId(idHospital);
        hospitalDoctor.setDoctorId(doctorId);

        return hospitalDoctorRepository.save(hospitalDoctor);
    }

    public HospitalDoctor getHospitalDoctorById(Long id) {
        return hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active hospitalDoctor found with ID: " + id));
    }

    public List<HospitalDoctor> getAllHospitalDoctorsByHospitalId(Long hospitalId) {
        return hospitalDoctorRepository.findAllByHospitalIdAndDeletedAtIsNull(hospitalId);
    }

    public Optional<HospitalDoctor> getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(Long doctorId, Long hospitalId) {
        return hospitalDoctorRepository.findByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);
    }

    public HospitalDoctor updateHospitalDoctor(Long id, HospitalDoctor hospitalDoctor, Long hospitalIdFromJwt) {

        hospitalDoctor.setHospitalId(hospitalIdFromJwt);

        doctorClient.getDoctorById(hospitalDoctor.getDoctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active Doctor found with ID: " + hospitalDoctor.getDoctorId()));

        HospitalDoctor existing = hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("No active hospitalDoctor found with ID: " + id));

        hospitalDoctor.setId(existing.getId());
        return hospitalDoctorRepository.save(hospitalDoctor);
    }

    public HospitalDoctor deleteHospitalDoctor(Long idHospitalDoctor, Long hospitalIdFromJwt) {
        HospitalDoctor hospitalDoctor = hospitalDoctorRepository.findByIdAndDeletedAtIsNull(idHospitalDoctor)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cannot delete. No active hospitalDoctor found with ID: " + idHospitalDoctor));

        if (!hospitalDoctor.getHospitalId().equals(hospitalIdFromJwt)) {
            throw new ForbiddenOperationException("Cannot delete. The hospitalDoctor does not belong to the hospital.");
        }

        hospitalDoctor.setDeletedAt(Instant.now());
        return (hospitalDoctorRepository.save(hospitalDoctor));
    }
}
