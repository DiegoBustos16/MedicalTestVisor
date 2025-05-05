package com.visor.patient_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.patient_microservice.entity.Patient;
import com.visor.patient_microservice.exception.BadRequestException;
import com.visor.patient_microservice.exception.ResourceNotFoundException;
import com.visor.patient_microservice.repository.PatientRepository;
import com.visor.patient_microservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PatientController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient samplePatient;

    @BeforeEach
    public void setUp() {
        samplePatient = Patient.builder()
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

    // --- POST /api/patients ---

    @Test
    void shouldCreatePatient() throws Exception {
        when(patientService.createPatient(any(Patient.class))).thenReturn(samplePatient);

        mockMvc.perform(post("/api/patients")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificationNumber").value(123456789));
    }

    @Test
    void shouldReturn400WhenPatientIsInvalid() throws Exception {
        samplePatient.setIdentificationNumber(null);

        mockMvc.perform(post("/api/patients")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCreating() throws Exception {
        when(patientService.createPatient(any(Patient.class))).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/api/patients")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/patients ---

    @Test
    void shouldGetAllPatients() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(samplePatient));

        mockMvc.perform(get("/api/patients")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificationNumber").value(123456789));
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingAll() throws Exception {
        when(patientService.getAllPatients()).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/patients")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/patients/{id} ---

    @Test
    void shouldGetPatientById() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(samplePatient);

        mockMvc.perform(get("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificationNumber").value(123456789));
    }

    @Test
    void shouldReturn400IfIdIsNotANumberWhenGettingById() throws Exception {
        mockMvc.perform(get("/api/patients/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfPatientNotFound() throws Exception {
        when(patientService.getPatientById(1L)).thenThrow(new ResourceNotFoundException("No active patient found with ID: 1"));

        mockMvc.perform(get("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingById() throws Exception {
        when(patientService.getPatientById(1L)).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/patients/exist/{id} ---

    @Test
    void shouldCheckIfPatientExists() throws Exception {
        when(patientService.existPatientById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/patients/exist/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void shouldReturn400IfIdIsNotANumberWhenGettingExist() throws Exception {
        mockMvc.perform(get("/api/patients/exist/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCheckingExist() throws Exception {
        when(patientService.existPatientById(1L)).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/patients/exist/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/patients/search ---
    @Test
    void shouldSearchPatients() throws Exception {
        Patient searchCriteria = Patient.builder()
                .firstName("Diego")
                .build();
        when(patientService.searchPatients(any(Patient.class))).thenReturn(List.of(samplePatient));

        mockMvc.perform(post("/api/patients/search")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificationNumber").value(123456789));
    }

    @Test
    void shouldReturn400IfSearchCriteriaIsInvalid() throws Exception {
        Patient searchCriteria = Patient.builder()
                .build();
        when(patientService.searchPatients(any(Patient.class))).thenThrow(new BadRequestException("Filter cannot be null or empty"));

        mockMvc.perform(post("/api/patients/search")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteria)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenSearching() throws Exception {
        Patient searchCriteria = Patient.builder()
                .firstName("Diego")
                .build();
        when(patientService.searchPatients(any(Patient.class))).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/api/patients/search")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteria)))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/patients/{id} ---

    @Test
    void shouldUpdatePatient() throws Exception {
        Patient updatedTestEntity = Patient.builder()
                .id(1L)
                .identificationNumber("1")
                .build();

        when(patientService.updatePatient(eq(1L), any(Patient.class))).thenReturn(updatedTestEntity);

        mockMvc.perform(put("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificationNumber").value(1));
    }

    @Test
    void shouldReturn400IfIdIsNotANumberWhenUpdating() throws Exception {
        mockMvc.perform(put("/api/patients/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfPatientNotFoundWhenUpdating() throws Exception {
        when(patientService.updatePatient(eq(1L), any(Patient.class))).thenThrow(new ResourceNotFoundException("No active Patient found with ID: 1"));

        mockMvc.perform(put("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenUpdating() throws Exception {
        when(patientService.updatePatient(eq(1L), any(Patient.class))).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(put("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/patients/{id} ---

    @Test
    void shouldDeletePatient() throws Exception {
        mockMvc.perform(delete("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400IfIdIsNotANumberWhenDeleting() throws Exception {
        mockMvc.perform(delete("/api/patients/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfPatientNotFoundWhenDeleting() throws Exception {
        doThrow(new ResourceNotFoundException("No active Patient found with ID: 1"))
                .when(patientService).deletePatient(eq(1L));

        mockMvc.perform(delete("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeleting() throws Exception {
        doThrow(new RuntimeException("Internal Server Error"))
                .when(patientService).deletePatient(eq(1L));

        mockMvc.perform(delete("/api/patients/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

}