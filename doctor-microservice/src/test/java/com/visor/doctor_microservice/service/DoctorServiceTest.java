package com.visor.doctor_microservice.service;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.exception.DuplicateResourceException;
import com.visor.doctor_microservice.exception.ResourceNotFoundException;
import com.visor.doctor_microservice.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .id(1L)
                .idKeycloak("keycloak-id")
                .licenseNumber("LIC123")
                .firstName("Diego")
                .lastName("Bustos")
                .dateOfBirth(LocalDate.of(2000, 2, 16))
                .gender("Male")
                .email("diego@example.com")
                .phoneNumber("+54912345678")
                .createdAt(Instant.now())
                .build();
    }

    // POST /doctors
    @Test
    void createDoctor_shouldReturnDoctor_whenSuccessful() {
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        Doctor result = doctorService.createDoctor(doctor);
        assertEquals("Diego", result.getFirstName());
    }

    @Test
    void createDoctor_shouldThrow_whenDuplicateKeycloakId() {
        when(doctorRepository.save(doctor)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate"));
        assertThrows(DuplicateResourceException.class, () -> doctorService.createDoctor(doctor));
    }

    // GET /doctors
    @Test
    void getAllDoctors_shouldReturnList() {
        when(doctorRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(doctor));
        List<Doctor> result = doctorService.getAllDoctors();
        assertEquals(1, result.size());
        assertEquals("Diego", result.get(0).getFirstName());
    }

    // GET /doctors/{id}
    @Test
    void getDoctorById_shouldReturnDoctor() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        Doctor result = doctorService.getDoctorById(1L);
        assertEquals("Diego", result.getFirstName());
    }

    @Test
    void getDoctorById_shouldThrow_whenNotFound() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(1L));
    }

    // GET /doctors/license/{licenseNumber}
    @Test
    void getDoctorByLicenseNumber_shouldReturnDoctor() {
        when(doctorRepository.findByLicenseNumberAndDeletedAtIsNull("LIC123")).thenReturn(Optional.of(doctor));
        Doctor result = doctorService.getDoctorByLicenseNumber("LIC123");
        assertEquals("Diego", result.getFirstName());
    }

    @Test
    void getDoctorByLicenseNumber_shouldThrow_whenNotFound() {
        when(doctorRepository.findByLicenseNumberAndDeletedAtIsNull("XYZ")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorByLicenseNumber("XYZ"));
    }

    // GET /doctors/keycloak/{idKeycloak}
    @Test
    void getDoctorIdByKeycloakId_shouldReturnId() {
        when(doctorRepository.findByIdKeycloakAndDeletedAtIsNull("keycloak-id")).thenReturn(Optional.of(doctor));
        Long id = doctorService.getDoctorIdByKeycloakId("keycloak-id");
        assertEquals(1L, id);
    }

    @Test
    void getDoctorIdByKeycloakId_shouldThrow_whenNotFound() {
        when(doctorRepository.findByIdKeycloakAndDeletedAtIsNull("wrong-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorIdByKeycloakId("wrong-id"));
    }

    // PATCH /doctors/{id}
    @Test
    void patchDoctor_shouldUpdateNonNullFields() {
        Doctor partial = new Doctor();
        partial.setFirstName("NotDiego");

        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doctor updated = doctorService.patchDoctor(1L, partial);
        assertEquals("NotDiego", updated.getFirstName());
    }

    @Test
    void patchDoctor_shouldThrow_whenNotFound() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> doctorService.patchDoctor(1L, new Doctor()));
    }

    // DELETE /doctors/{id}
    @Test
    void deleteDoctor_shouldSetDeletedAt() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        doctorService.deleteDoctor(1L);
        assertNotNull(doctor.getDeletedAt());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void deleteDoctor_shouldThrow_whenNotFound() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> doctorService.deleteDoctor(1L));
    }
}
