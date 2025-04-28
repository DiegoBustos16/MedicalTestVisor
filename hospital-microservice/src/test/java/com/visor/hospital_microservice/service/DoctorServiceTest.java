package com.visor.hospital_microservice.service;


import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorClient doctorClient;

    @Mock
    private HospitalDoctorRepository hospitalDoctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Nested
    @DisplayName("getAllDoctorsByHospital")
    class GetAllDoctorsByHospital {

        @Test
        @DisplayName("should return list of doctors when doctors are found")
        void shouldReturnListOfDoctorsWhenFound() {

            Long hospitalId = 1L;

            HospitalDoctor relation1 = new HospitalDoctor();
            relation1.setDoctorId(101L);

            HospitalDoctor relation2 = new HospitalDoctor();
            relation2.setDoctorId(102L);

            DoctorDTO doctor1 = DoctorDTO.builder()
                    .id(101L)
                    .firstName("Diego")
                    .build();

            DoctorDTO doctor2 = DoctorDTO.builder()
                    .id(102L)
                    .firstName("Jane")
                    .build();

            when(hospitalDoctorRepository.findAllByHospitalIdAndDeletedAtIsNull(hospitalId))
                    .thenReturn(Arrays.asList(relation1, relation2));

            when(doctorClient.getDoctorById(101L)).thenReturn(Optional.of(doctor1));
            when(doctorClient.getDoctorById(102L)).thenReturn(Optional.of(doctor2));


            List<DoctorDTO> doctors = doctorService.getAllDoctorsByHospital(hospitalId);


            assertThat(doctors).hasSize(2);
            assertThat(doctors.get(0).getId()).isEqualTo(101L);
            assertThat(doctors.get(1).getId()).isEqualTo(102L);

            verify(hospitalDoctorRepository, times(1)).findAllByHospitalIdAndDeletedAtIsNull(hospitalId);
            verify(doctorClient, times(1)).getDoctorById(101L);
            verify(doctorClient, times(1)).getDoctorById(102L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when doctor not found")
        void shouldThrowResourceNotFoundExceptionWhenDoctorNotFound() {

            Long hospitalId = 1L;

            HospitalDoctor relation = new HospitalDoctor();
            relation.setDoctorId(101L);

            when(hospitalDoctorRepository.findAllByHospitalIdAndDeletedAtIsNull(hospitalId))
                    .thenReturn(List.of(relation));

            when(doctorClient.getDoctorById(101L)).thenReturn(Optional.empty());


            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> doctorService.getAllDoctorsByHospital(hospitalId)
            );

            assertThat(exception.getMessage()).isEqualTo("No active doctor found with ID: 101");

            verify(hospitalDoctorRepository, times(1)).findAllByHospitalIdAndDeletedAtIsNull(hospitalId);
            verify(doctorClient, times(1)).getDoctorById(101L);
        }

        @Test
        @DisplayName("should return empty list when no relations are found")
        void shouldReturnEmptyListWhenNoRelationsFound() {

            Long hospitalId = 1L;

            when(hospitalDoctorRepository.findAllByHospitalIdAndDeletedAtIsNull(hospitalId))
                    .thenReturn(List.of());


            List<DoctorDTO> doctors = doctorService.getAllDoctorsByHospital(hospitalId);


            assertThat(doctors).isEmpty();

            verify(hospitalDoctorRepository, times(1)).findAllByHospitalIdAndDeletedAtIsNull(hospitalId);
            verifyNoInteractions(doctorClient);
        }
    }
}