package com.visor.patient_microservice.service;

import com.visor.patient_microservice.entity.Patient;
import com.visor.patient_microservice.exception.DuplicateResourceException;
import com.visor.patient_microservice.exception.ResourceNotFoundException;
import com.visor.patient_microservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatienyServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    private Patient patient;

    @BeforeEach
    public void setUp() {
        patient = Patient.builder()
                .id(1L)
                .identificationNumber("123456789")
                .firstName("Diego")
                .lastName("Bustos")
                .dateOfBirth(LocalDate.parse("2000-02-16"))
                .gender("Male")
                .email("diegombustos16@gmail.com")
                .phoneNumber("+54912345678")
                .build();
    }

    @Nested
    @DisplayName("Create Patient")
    class CreatePatientTests {

        @Test
        @DisplayName("should return saved patient when successful")
        void createPatient_shouldReturnSavedPatient_whenSuccessful() {
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            Patient result = patientService.createPatient(patient);

            assertThat(result).isNotNull();
            assertThat(result.getIdentificationNumber()).isEqualTo("123456789");
            verify(patientRepository, times(1)).save(patient);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when patient already exists")
        void createPatient_shouldThrowDuplicateResourceException_whenPatientAlreadyExists() {
            when(patientRepository.save(any(Patient.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

            assertThrows(DuplicateResourceException.class, () -> patientService.createPatient(patient));

            verify(patientRepository, times(1)).save(patient);
        }
    }

    @Nested
    @DisplayName("Get all Patients")
    class GetAllPatientsTests {

        @Test
        @DisplayName("should return list of patients when successful")
        void getAllPatients_shouldReturnListOfPatients_whenSuccessful() {
            when(patientRepository.findAll()).thenReturn(List.of(patient));

            List<Patient> result = patientService.getAllPatients();

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getIdentificationNumber()).isEqualTo("123456789");
            verify(patientRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Get Patient by ID")
    class GetPatientByIdTests {

        @Test
        @DisplayName("should return patient when found")
        void getPatientById_shouldReturnPatient_whenFound() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.of(patient));

            Patient result = patientService.getPatientById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getIdentificationNumber()).isEqualTo("123456789");
            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when patient not found")
        void getPatientById_shouldThrowResourceNotFoundException_whenNotFound() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(1L));

            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }
    }

    @Nested
    @DisplayName("Check if Patient Exists")
    class CheckIfPatientExistsTests {

        @Test
        @DisplayName("should return true when patient exists")
        void existPatientById_shouldReturnTrue_whenPatientExists() {
            when(patientRepository.existsPatientById(1L)).thenReturn(true);

            boolean result = patientService.existPatientById(1L);

            assertThat(result).isTrue();
            verify(patientRepository, times(1)).existsPatientById(1L);
        }

        @Test
        @DisplayName("should return false when patient does not exist")
        void existPatientById_shouldReturnFalse_whenPatientDoesNotExist() {
            when(patientRepository.existsPatientById(1L)).thenReturn(false);

            boolean result = patientService.existPatientById(1L);

            assertThat(result).isFalse();
            verify(patientRepository, times(1)).existsPatientById(1L);
        }
    }

    @Nested
    @DisplayName("Search Patient")
    class SearchPatientTests {

        @Test
        @DisplayName("should return list of patients when successful")
        void searchPatients_shouldReturnListOfPatients_whenSuccessful() {
            Patient filter = Patient.builder()
                    .firstName("Diego")
                    .build();

            when(patientRepository.findAll(any(Example.class))).thenReturn(List.of(patient));

            List<Patient> result = patientService.searchPatients(filter);

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Diego");
            verify(patientRepository, times(1)).findAll(any(Example.class));
        }
    }

    @Nested
    @DisplayName("Update Patient")
    class UpdatePatientTests {

        @Test
        @DisplayName("should return updated patient when successful")
        void updatePatient_shouldReturnUpdatedPatient_whenSuccessful() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.of(patient));
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            Patient result = patientService.updatePatient(1L, patient);

            assertThat(result).isNotNull();
            assertThat(result.getIdentificationNumber()).isEqualTo("123456789");
            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
            verify(patientRepository, times(1)).save(patient);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when patient not found")
        void updatePatient_shouldThrowResourceNotFoundException_whenNotFound() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.updatePatient(1L, patient));

            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }
    }

    @Nested
    @DisplayName("Delete Patient")
    class DeletePatientTests {

        @Test
        @DisplayName("should delete patient when successful")
        void deletePatient_shouldDeletePatient_whenSuccessful() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.of(patient));

            patientService.deletePatient(1L);

            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
            verify(patientRepository, times(1)).save(any(Patient.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when patient not found")
        void deletePatient_shouldThrowResourceNotFoundException_whenNotFound() {
            when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.deletePatient(1L));

            verify(patientRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        }
    }

}
