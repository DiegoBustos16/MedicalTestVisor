package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.exception.DuplicateResourceException;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceTest {

    @InjectMocks
    private HospitalService hospitalService;

    @Mock
    private HospitalRepository hospitalRepository;

    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospital = Hospital.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .name("Hospital Central")
                .address("Mendoza")
                .phoneNumber("+54912345678")
                .email("diegombustos16@gmail.com")
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Create Hospital")
    class CreateHospitalTests {

        @Test
        @DisplayName("should return saved hospital when successful")
        void createHospital_shouldReturnHospital_whenSuccessful() {
            when(hospitalRepository.save(hospital)).thenReturn(hospital);

            Hospital result = hospitalService.createHospital(hospital);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Hospital Central");
            verify(hospitalRepository, times(1)).save(hospital);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when keycloakId already exists")
        void createHospital_shouldThrowDuplicateResourceException_whenDuplicateKeycloakId() {
            when(hospitalRepository.save(hospital)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate"));

            assertThrows(DuplicateResourceException.class, () -> hospitalService.createHospital(hospital));
            verify(hospitalRepository, times(1)).save(hospital);
        }
    }

    @Nested
    @DisplayName("Get Hospitals")
    class GetHospitalsTests {

        @Test
        @DisplayName("should return list of hospitals")
        void getAllHospitals_shouldReturnList() {
            when(hospitalRepository.findAll()).thenReturn(List.of(hospital));

            List<Hospital> hospitals = hospitalService.getAllHospitals();

            assertThat(hospitals)
                    .asInstanceOf(LIST)
                    .hasSize(1);
            assertThat(hospitals.get(0).getName()).isEqualTo("Hospital Central");
            verify(hospitalRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return hospital by ID")
        void getHospitalById_shouldReturnHospital() {
            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(hospital));

            Hospital result = hospitalService.getHospitalById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(hospitalRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when hospital by ID not found")
        void getHospitalById_shouldThrow_whenNotFound() {
            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> hospitalService.getHospitalById(1L));
            verify(hospitalRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("should return hospital ID by Keycloak ID")
        void getHospitalIdByKeycloakId_shouldReturnId() {
            when(hospitalRepository.findByIdKeycloakAndDeletedAtIsNull("keycloak-id")).thenReturn(Optional.of(hospital));

            Long result = hospitalService.getHospitalIdByKeycloakId("keycloak-id");

            assertThat(result).isEqualTo(1L);
            verify(hospitalRepository, times(1)).findByIdKeycloakAndDeletedAtIsNull("keycloak-id");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when Keycloak ID not found")
        void getHospitalIdByKeycloakId_shouldThrow_whenNotFound() {
            when(hospitalRepository.findByIdKeycloakAndDeletedAtIsNull("invalid-id")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> hospitalService.getHospitalIdByKeycloakId("invalid-id"));
            verify(hospitalRepository, times(1)).findByIdKeycloakAndDeletedAtIsNull("invalid-id");
        }
    }

    @Nested
    @DisplayName("Patch Hospital")
    class PatchHospitalTests {

        @Test
        @DisplayName("should update non-null fields of hospital")
        void patchHospital_shouldUpdateFields() {
            Hospital partial = new Hospital();
            partial.setName("Updated Hospital");

            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(hospital));
            when(hospitalRepository.save(any(Hospital.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Hospital updated = hospitalService.patchHospital(1L, partial);

            assertThat(updated.getName()).isEqualTo("Updated Hospital");
            assertThat(updated.getPhoneNumber()).isEqualTo("+54912345678"); // unchanged field
            verify(hospitalRepository).save(hospital);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when hospital to patch not found")
        void patchHospital_shouldThrow_whenNotFound() {
            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> hospitalService.patchHospital(1L, new Hospital()));
            verify(hospitalRepository, never()).save(any(Hospital.class));
        }
    }

    @Nested
    @DisplayName("Delete Hospital")
    class DeleteHospitalTests {

        @Test
        @DisplayName("should set deletedAt timestamp when deleting hospital")
        void deleteHospital_shouldSetDeletedAt() {
            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(hospital));

            hospitalService.deleteHospital(1L);

            assertThat(hospital.getDeletedAt()).isNotNull();
            verify(hospitalRepository, times(1)).save(hospital);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deleting non-existent hospital")
        void deleteHospital_shouldThrow_whenNotFound() {
            when(hospitalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> hospitalService.deleteHospital(1L));
            verify(hospitalRepository, never()).save(any(Hospital.class));
        }
    }
}
