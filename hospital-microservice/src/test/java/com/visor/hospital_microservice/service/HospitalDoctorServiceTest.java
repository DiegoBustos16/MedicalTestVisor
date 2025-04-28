package com.visor.hospital_microservice.service;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class HospitalDoctorServiceTest {

    @Mock
    private HospitalDoctorRepository hospitalDoctorRepository;
    @Mock
    private DoctorClient doctorClient;

    @InjectMocks
    private HospitalDoctorService hospitalDoctorService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }


    @Nested
    @DisplayName("Create HospitalDoctor")
    class CreateHospitalDoctorTests {

        @Test
        @DisplayName("should save HospitalDoctor when Doctor exists")
        void createHospitalDoctor_shouldSaveHospitalDoctor_whenDoctorExists() {
            Long doctorId = 1L;
            Long hospitalId = 10L;

            given(doctorClient.getDoctorById(doctorId)).willReturn(Optional.of(buildDoctorDTO(doctorId)));
            HospitalDoctor saved = new HospitalDoctor();
            saved.setId(100L);
            saved.setDoctorId(doctorId);
            saved.setHospitalId(hospitalId);

            given(hospitalDoctorRepository.save(any(HospitalDoctor.class))).willReturn(saved);

            HospitalDoctor result = hospitalDoctorService.createHospitalDoctor(doctorId, hospitalId);

            assertThat(result)
                    .isNotNull()
                    .extracting(HospitalDoctor::getDoctorId, HospitalDoctor::getHospitalId)
                    .containsExactly(doctorId, hospitalId);

            verify(hospitalDoctorRepository).save(any(HospitalDoctor.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when Doctor does not exist")
        void createHospitalDoctor_shouldThrow_whenDoctorDoesNotExist() {
            Long doctorId = 2L;
            Long hospitalId = 20L;

            given(doctorClient.getDoctorById(doctorId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalDoctorService.createHospitalDoctor(doctorId, hospitalId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Doctor found with ID: " + doctorId);
        }
    }

    @Nested
    @DisplayName("Get HospitalDoctor by ID")
    class GetHospitalDoctorByIdTests {

        @Test
        @DisplayName("should return HospitalDoctor when exists")
        void getHospitalDoctorById_shouldReturnHospitalDoctor_whenExists() {
            Long id = 5L;
            HospitalDoctor hospitalDoctor = new HospitalDoctor();
            hospitalDoctor.setId(id);

            given(hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(hospitalDoctor));

            HospitalDoctor result = hospitalDoctorService.getHospitalDoctorById(id);

            assertThat(result)
                    .isNotNull()
                    .extracting(HospitalDoctor::getId)
                    .isEqualTo(id);

            verify(hospitalDoctorRepository).findByIdAndDeletedAtIsNull(id);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void getHospitalDoctorById_shouldThrow_whenNotFound() {
            Long id = 6L;

            given(hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalDoctorService.getHospitalDoctorById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active hospitalDoctor found with ID: " + id);
        }
    }

    @Nested
    @DisplayName("Get all HospitalDoctors by Hospital ID")
    class GetAllHospitalDoctorsByHospitalIdTests {

        @Test
        @DisplayName("should return list of HospitalDoctors")
        void getAllHospitalDoctorsByHospitalId_shouldReturnList() {
            Long hospitalId = 10L;
            HospitalDoctor hd = new HospitalDoctor();
            hd.setId(1L);

            given(hospitalDoctorRepository.findAllByHospitalIdAndDeletedAtIsNull(hospitalId))
                    .willReturn(List.of(hd));

            List<HospitalDoctor> result = hospitalDoctorService.getAllHospitalDoctorsByHospitalId(hospitalId);

            assertThat(result)
                    .isNotEmpty()
                    .hasSize(1)
                    .extracting(HospitalDoctor::getId)
                    .containsExactly(1L);

            verify(hospitalDoctorRepository).findAllByHospitalIdAndDeletedAtIsNull(hospitalId);
        }
    }

    @Nested
    @DisplayName("Get HospitalDoctor by Doctor ID and Hospital ID")
    class GetHospitalDoctorByDoctorIdAndHospitalIdTests {

        @Test
        @DisplayName("should return optional HospitalDoctor")
        void getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull_shouldReturnOptional() {
            Long doctorId = 7L;
            Long hospitalId = 8L;
            HospitalDoctor hd = new HospitalDoctor();
            hd.setId(99L);

            given(hospitalDoctorRepository.findByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId))
                    .willReturn(Optional.of(hd));

            Optional<HospitalDoctor> result = hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);

            assertThat(result)
                    .isPresent()
                    .get()
                    .extracting(HospitalDoctor::getId)
                    .isEqualTo(99L);

            verify(hospitalDoctorRepository).findByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);
        }
    }

    @Nested
    @DisplayName("Update HospitalDoctor")
    class UpdateHospitalDoctorTests {

        @Test
        @DisplayName("should update HospitalDoctor when valid")
        void updateHospitalDoctor_shouldUpdate_whenValid() {
            Long id = 1L;
            Long hospitalIdFromJwt = 10L;
            HospitalDoctor input = new HospitalDoctor();
            input.setDoctorId(2L);

            given(doctorClient.getDoctorById(input.getDoctorId())).willReturn(Optional.of(buildDoctorDTO(input.getDoctorId())));

            HospitalDoctor existing = new HospitalDoctor();
            existing.setId(id);

            given(hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(hospitalDoctorRepository.save(any(HospitalDoctor.class))).willReturn(existing);

            HospitalDoctor result = hospitalDoctorService.updateHospitalDoctor(id, input, hospitalIdFromJwt);

            assertThat(result)
                    .isNotNull()
                    .extracting(HospitalDoctor::getId)
                    .isEqualTo(id);

            verify(hospitalDoctorRepository).save(any(HospitalDoctor.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when Doctor not found")
        void updateHospitalDoctor_shouldThrow_whenDoctorNotFound() {
            Long id = 1L;
            Long hospitalIdFromJwt = 10L;
            HospitalDoctor input = new HospitalDoctor();
            input.setDoctorId(999L);

            given(doctorClient.getDoctorById(input.getDoctorId())).willReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalDoctorService.updateHospitalDoctor(id, input, hospitalIdFromJwt))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Doctor found with ID: 999");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when HospitalDoctor not found")
        void updateHospitalDoctor_shouldThrow_whenHospitalDoctorNotFound() {
            Long id = 1L;
            Long hospitalIdFromJwt = 10L;
            HospitalDoctor input = new HospitalDoctor();
            input.setDoctorId(1L);

            given(doctorClient.getDoctorById(input.getDoctorId())).willReturn(Optional.of(buildDoctorDTO(input.getDoctorId())));
            given(hospitalDoctorRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalDoctorService.updateHospitalDoctor(id, input, hospitalIdFromJwt))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active hospitalDoctor found with ID: " + id);
        }
    }

    private DoctorDTO buildDoctorDTO(Long id) {
        return DoctorDTO.builder()
                .id(id)
                .firstName("Diego")
                .lastName("Bustos")
                .email("diegombustos16@gmail.com")
                .licenseNumber("ABC123")
                .build();
    }
}
