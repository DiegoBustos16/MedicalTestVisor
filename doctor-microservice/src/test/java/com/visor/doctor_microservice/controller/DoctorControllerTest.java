package com.visor.doctor_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.exception.DuplicateResourceException;
import com.visor.doctor_microservice.exception.ResourceNotFoundException;
import com.visor.doctor_microservice.repository.DoctorRepository;
import com.visor.doctor_microservice.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DoctorController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Doctor sampleDoctor;

    @BeforeEach
    void setUp() {
        sampleDoctor = Doctor.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .firstName("Diego")
                .lastName("Bustos")
                .email("diegombustos16@gmail.com")
                .licenseNumber("ABC123")
                .build();
    }

    // --- GET /api/doctors ---

    @Test
    void shouldGetAllDoctors() throws Exception {
        when(doctorService.getAllDoctors()).thenReturn(List.of(sampleDoctor));

        mockMvc.perform(get("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Diego"));
    }

    // --- GET /api/doctors/{id} ---

    @Test
    void shouldGetDoctorById() throws Exception {
        when(doctorService.getDoctorById(1L)).thenReturn(sampleDoctor);

        mockMvc.perform(get("/api/doctors/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("diegombustos16@gmail.com"));
    }

    @Test
    void shouldReturn404IfDoctorNotFoundById() throws Exception {
        when(doctorService.getDoctorById(99L)).thenThrow(new ResourceNotFoundException("No active doctor found with ID: 99"));

        mockMvc.perform(get("/api/doctors/99")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/doctors/license/{licenseNumber} ---

    @Test
    void shouldGetDoctorByLicense() throws Exception {
        when(doctorService.getDoctorByLicenseNumber("ABC123")).thenReturn(sampleDoctor);

        mockMvc.perform(get("/api/doctors/license/ABC123")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idKeycloak").value("keycloak-id-123"));
    }

    @Test
    void shouldReturn404IfDoctorNotFoundByLicense() throws Exception {
        when(doctorService.getDoctorByLicenseNumber("XYZ999")).thenThrow(new ResourceNotFoundException("No active doctor found with license number: XYZ999"));

        mockMvc.perform(get("/api/doctors/license/XYZ999")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/doctors/exist/{keycloakId} ---

    @Test
    void shouldGetDoctorIdByKeycloakId() throws Exception {
        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(get("/api/doctors/exist/keycloak-id-123")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void shouldReturn404IfDoctorNotFoundByKeycloakId() throws Exception {
        when(doctorService.getDoctorIdByKeycloakId("unknown-keycloak-id")).thenThrow(new ResourceNotFoundException("No active doctor found with Keycloak ID: unknown-keycloak-id"));

        mockMvc.perform(get("/api/doctors/exist/unknown-keycloak-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    // --- PATCH /api/doctors ---

    @Test
    void shouldUpdateDoctor() throws Exception {
        Doctor updatedDoctor = Doctor.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .licenseNumber("ABC123")
                .firstName("Updated")
                .lastName("Bustos")
                .email("diegombustos16@gmail.com")
                .build();

        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(doctorService.patchDoctor(eq(1L), any(Doctor.class))).thenReturn(updatedDoctor);

        mockMvc.perform(patch("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDoctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void shouldReturn400IfInvalidDoctorData() throws Exception {
        Doctor invalidDoctor = Doctor.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .licenseNumber("ABC123")
                .firstName("")
                .lastName("Bustos")
                .email("invalidemail")
                .build();

        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(patch("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDoctor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenUpdating() throws Exception {
        mockMvc.perform(patch("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDoctor)))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /api/doctors ---

    @Test
    void shouldDeleteDoctor() throws Exception {
        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(delete("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404IfDoctorNotFoundWhenDeleting() throws Exception {
        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123"))
                .thenThrow(new ResourceNotFoundException("No active doctor found with Keycloak ID: keycloak-id-123"));

        mockMvc.perform(delete("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenDeleting() throws Exception {
        mockMvc.perform(delete("/api/doctors"))
                .andExpect(status().isForbidden());
    }
}