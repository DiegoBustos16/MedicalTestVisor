package com.visor.hospital_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalRepository;
import com.visor.hospital_microservice.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(controllers = HospitalController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalService hospitalService;

    @MockitoBean
    private HospitalRepository hospitalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Hospital sampleHospital;

    @BeforeEach
    void setUp() {
        sampleHospital = Hospital.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .name("Central Hospital")
                .address("Mendoza")
                .phoneNumber("+54912345678")
                .email("diegombustos16@gmail.com")
                .createdAt(Instant.now())
                .build();
    }

    // --- GET /api/hospitals ---

    @Test
    void shouldGetAllHospitals() throws Exception {
        when(hospitalService.getAllHospitals()).thenReturn(List.of(sampleHospital));

        mockMvc.perform(get("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Central Hospital"));
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingAllHospitals() throws Exception {
        when(hospitalService.getAllHospitals()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/hospitals/{id} ---

    @Test
    void shouldGetHospitalById() throws Exception {
        when(hospitalService.getHospitalById(1L)).thenReturn(sampleHospital);

        mockMvc.perform(get("/api/hospitals/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Mendoza"));
    }

    @Test
    void shouldReturn400IfIdIsNotLong() throws Exception {
        mockMvc.perform(get("/api/hospitals/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfHospitalNotFoundById() throws Exception {
        when(hospitalService.getHospitalById(99L)).thenThrow(new ResourceNotFoundException("No active doctor found with ID: 99"));

        mockMvc.perform(get("/api/hospitals/99")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingHospitalById() throws Exception {
        when(hospitalService.getHospitalById(1L)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/hospitals/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- PATCH /api/hospitals/{id} ---

    @Test
    void shouldUpdateDoctor() throws Exception {
        Hospital updatedDoctor = Hospital.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .name("Updated")
                .address("Mendoza")
                .phoneNumber("+54912345678")
                .email("diegombustos16@gmail.com")
                .createdAt(Instant.now())
                .build();

        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalService.patchHospital(eq(1L), any(Hospital.class))).thenReturn(updatedDoctor);

        mockMvc.perform(patch("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDoctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldReturn400IfInvalidHospitalData() throws Exception {
        Hospital invalidHospital = Hospital.builder()
                .id(1L)
                .idKeycloak("keycloak-id-123")
                .name("")
                .address("Mendoza")
                .phoneNumber("+54912345678")
                .email("invalidemail")
                .createdAt(Instant.now())
                .build();

        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(patch("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHospital)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenUpdating() throws Exception {
        mockMvc.perform(patch("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospital)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfHospitalNotFoundWhenUpdating() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123"))
                .thenThrow(new ResourceNotFoundException("No active hospital found with Keycloak ID: keycloak-id-123"));

        mockMvc.perform(patch("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospital)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenUpdatingDoctor() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalService.patchHospital(eq(1L), any(Hospital.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(patch("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospital)))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/hospitals ---

    @Test
    void shouldDeleteHospital() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(delete("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenDeleting() throws Exception {
        mockMvc.perform(delete("/api/hospitals"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfHospitalNotFoundWhenDeleting() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123"))
                .thenThrow(new ResourceNotFoundException("No active hospital found with Keycloak ID: keycloak-id-123"));

        mockMvc.perform(delete("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeletingHospital() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        doThrow(new RuntimeException("Unexpected error")).when(hospitalService).deleteHospital(1L);

        mockMvc.perform(delete("/api/hospitals")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }
}
