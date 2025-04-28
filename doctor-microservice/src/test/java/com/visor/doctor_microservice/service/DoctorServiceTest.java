package com.visor.doctor_microservice.service;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.exception.DuplicateResourceException;
import com.visor.doctor_microservice.exception.ResourceNotFoundException;
import com.visor.doctor_microservice.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
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

    @Nested
    @DisplayName("Create Doctor")
    class CreateDoctorTests {

        @Test
        @DisplayName("should return saved doctor when successful")
        void createDoctor_shouldReturnDoctor_whenSuccessful() {
            when(doctorRepository.save(doctor)).thenReturn(doctor);

            Doctor result = doctorService.createDoctor(doctor);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Diego");
            verify(doctorRepository, times(1)).save(doctor);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when keycloakId already exists")
        void createDoctor_shouldThrowDuplicateResourceException_whenDuplicateKeycloakId() {
            when(doctorRepository.save(doctor)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate"));

            assertThrows(DuplicateResourceException.class, () -> doctorService.createDoctor(doctor));
            verify(doctorRepository, times(1)).save(doctor);
        }
    }

    @Nested
    @DisplayName("Get Doctors")
    class GetDoctorsTests {

        @Test
        @DisplayName("should return list of doctors")
        void getAllDoctors_shouldReturnList() {
            when(doctorRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(doctor));

            List<Doctor> result = doctorService.getAllDoctors();

            assertThat(result)
                    .asInstanceOf(LIST)
                    .hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Diego");
            verify(doctorRepository, times(1)).findAllByDeletedAtIsNull();
        }

        @Test
        @DisplayName("should return doctor by ID")
        void getDoctorById_shouldReturnDoctor() {
            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));

            Doctor result = doctorService.getDoctorById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(doctorRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when doctor by ID not found")
        void getDoctorById_shouldThrow_whenNotFound() {
            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(1L));
            verify(doctorRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("should return doctor by License Number")
        void getDoctorByLicenseNumber_shouldReturnDoctor() {
            when(doctorRepository.findByLicenseNumberAndDeletedAtIsNull("LIC123")).thenReturn(Optional.of(doctor));

            Doctor result = doctorService.getDoctorByLicenseNumber("LIC123");

            assertThat(result).isNotNull();
            assertThat(result.getLicenseNumber()).isEqualTo("LIC123");
            verify(doctorRepository, times(1)).findByLicenseNumberAndDeletedAtIsNull("LIC123");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when license number not found")
        void getDoctorByLicenseNumber_shouldThrow_whenNotFound() {
            when(doctorRepository.findByLicenseNumberAndDeletedAtIsNull("XYZ")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorByLicenseNumber("XYZ"));
            verify(doctorRepository, times(1)).findByLicenseNumberAndDeletedAtIsNull("XYZ");
        }

        @Test
        @DisplayName("should return doctor ID by Keycloak ID")
        void getDoctorIdByKeycloakId_shouldReturnId() {
            when(doctorRepository.findByIdKeycloakAndDeletedAtIsNull("keycloak-id")).thenReturn(Optional.of(doctor));

            Long id = doctorService.getDoctorIdByKeycloakId("keycloak-id");

            assertThat(id).isEqualTo(1L);
            verify(doctorRepository, times(1)).findByIdKeycloakAndDeletedAtIsNull("keycloak-id");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when Keycloak ID not found")
        void getDoctorIdByKeycloakId_shouldThrow_whenNotFound() {
            when(doctorRepository.findByIdKeycloakAndDeletedAtIsNull("wrong-id")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorIdByKeycloakId("wrong-id"));
            verify(doctorRepository, times(1)).findByIdKeycloakAndDeletedAtIsNull("wrong-id");
        }
    }

    @Nested
    @DisplayName("Patch Doctor")
    class PatchDoctorTests {

        @Test
        @DisplayName("should update non-null fields of doctor")
        void patchDoctor_shouldUpdateFields() {
            Doctor partial = new Doctor();
            partial.setFirstName("UpdatedName");

            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
            when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Doctor updated = doctorService.patchDoctor(1L, partial);

            assertThat(updated.getFirstName()).isEqualTo("UpdatedName");
            assertThat(updated.getLastName()).isEqualTo("Bustos"); // unchanged field
            verify(doctorRepository, times(1)).save(doctor);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when doctor to patch not found")
        void patchDoctor_shouldThrow_whenNotFound() {
            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> doctorService.patchDoctor(1L, new Doctor()));
            verify(doctorRepository, never()).save(any(Doctor.class));
        }
    }

    @Nested
    @DisplayName("Delete Doctor")
    class DeleteDoctor {

        @Test
        @DisplayName("should set deletedAt and save the doctor when found")
        void shouldSetDeletedAtAndSaveDoctorWhenFound() {
            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));

            doctorService.deleteDoctor(1L);

            assertNotNull(doctor.getDeletedAt());
            verify(doctorRepository).save(doctor);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when doctor not found")
        void shouldThrowExceptionWhenDoctorNotFound() {
            when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> doctorService.deleteDoctor(1L));
        }
    }

}
