package com.visor.hospital_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.exception.ResourceNotFoundException;
import com.visor.hospital_microservice.repository.HospitalDoctorRepository;
import com.visor.hospital_microservice.repository.HospitalRepository;
import com.visor.hospital_microservice.service.HospitalDoctorService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HospitalDoctorController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class HospitalDoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalDoctorService hospitalDoctorService;

    @MockitoBean
    private HospitalDoctorRepository hospitalDoctorRepository;

    @MockitoBean
    private HospitalRepository hospitalRepository;

    @MockitoBean
    private HospitalService hospitalService;

    @MockitoBean
    private DoctorClient doctorClient;

    @Autowired
    private ObjectMapper objectMapper;

    private HospitalDoctor sampleHospitalDoctor;

    @BeforeEach
    void setUp() {
        sampleHospitalDoctor = HospitalDoctor.builder()
                .id(1L)
                .hospitalId(1L)
                .doctorId(1L)
                .createdAt(Instant.now())
                .build();
    }

// --- POST /api/hospitals/hospital-doctor ---

    @Test
    void shouldCreateHospitalDoctor() throws Exception {
        when(hospitalDoctorService.createHospitalDoctor(1L, 1L)).thenReturn(sampleHospitalDoctor);
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(post("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .param("doctorId", "1")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(1L));
    }

    @Test
    void shouldReturn400IfInvalidDoctorData() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(post("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .param("doctorId", "abc")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenCreating() throws Exception {
        mockMvc.perform(patch("/api/hospitals/hospital-doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospitalDoctor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfDoctorNotFoundWhenUpdating() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123"))
                .thenThrow(new ResourceNotFoundException("No active hospital found with Keycloak ID: keycloak-id-123"));

        mockMvc.perform(post("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .param("doctorId", "99")
                        )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCreatingHospitalDoctor() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        doThrow(new RuntimeException("Unexpected error")).when(hospitalDoctorService).createHospitalDoctor(1L, 1L);

        mockMvc.perform(delete("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/hospitals/hospital-doctor ---

    @Test
    void shouldGetAllHospitalDoctorOfHospital() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalDoctorService.getAllHospitalDoctorsByHospitalId(1L)).thenReturn(List.of(sampleHospitalDoctor));

        mockMvc.perform(get("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value("1"));
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingAllHospitalDoctorOfHospital() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        doThrow(new RuntimeException("Unexpected error")).when(hospitalDoctorService).getAllHospitalDoctorsByHospitalId(1L);

        mockMvc.perform(delete("/api/hospitals/hospital-doctor")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/hospitals/hospital-doctor/{id} ---

    @Test
    void shouldGetHospitalDoctorById() throws Exception {
        when(hospitalDoctorService.getHospitalDoctorById(1L)).thenReturn(sampleHospitalDoctor);

        mockMvc.perform(get("/api/hospitals/hospital-doctor/1")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("sub", "keycloak-id-123");
                    jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(1L));
    }

    @Test
    void shouldReturn400IfIdIsNotLong() throws Exception {
        mockMvc.perform(get("/api/hospitals/hospital-doctor/abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfHospitalDoctorNotFound() throws Exception {
        when(hospitalDoctorService.getHospitalDoctorById(1L))
                .thenThrow(new ResourceNotFoundException("No active hospitalDoctor found with ID: 1"));

        mockMvc.perform(get("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingHospitalDoctorById() throws Exception {
        when(hospitalDoctorService.getHospitalDoctorById(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/hospitals/hospital-doctor/exist ---

    @Test
    void shouldGetHospitalDoctorByDoctorIdAndHospitalId() throws Exception {
        when(hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(1L, 1L))
                .thenReturn(java.util.Optional.of(sampleHospitalDoctor));

        mockMvc.perform(get("/api/hospitals/hospital-doctor/exist")
                        .param("doctorId", "1")
                        .param("hospitalId", "1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void shouldReturn400IfInvalidDoctorIdOrHospitalId() throws Exception {
        mockMvc.perform(get("/api/hospitals/hospital-doctor/exist")
                        .param("doctorId", "abc")
                        .param("hospitalId", "abc")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingHospitalDoctorByDoctorIdAndHospitalId() throws Exception {
        when(hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(1L, 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/hospitals/hospital-doctor/exist")
                        .param("doctorId", "1")
                        .param("hospitalId", "1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/hospitals/hospital-doctor/{id} ---
    @Test
    void shouldUpdateHospitalDoctor() throws Exception {
        HospitalDoctor updatedHospitalDoctor = HospitalDoctor.builder()
                .hospitalId(1L)
                .doctorId(2L)
                .createdAt(Instant.now())
                .build();

        when(hospitalDoctorService.updateHospitalDoctor(eq(1L), any(HospitalDoctor.class), eq(1L))).thenReturn(updatedHospitalDoctor);
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(put("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedHospitalDoctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(2L));
    }

    @Test
    void shouldReturn400IfInvalidUpdateData() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(put("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorId\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenUpdating() throws Exception {
        mockMvc.perform(put("/api/hospitals/hospital-doctor/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospitalDoctor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfHospitalDoctorNotFoundWhenUpdating() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalDoctorService.updateHospitalDoctor(eq(1L), any(HospitalDoctor.class), eq(1L)))
                .thenThrow(new ResourceNotFoundException("No active hospitalDoctor found with ID: "+1L));

        mockMvc.perform(put("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospitalDoctor)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenUpdatingHospitalDoctor() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalDoctorService.updateHospitalDoctor(eq(1L), any(HospitalDoctor.class), eq(1L)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospitalDoctor)))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/hospitals/hospital-doctor/{id} ---

    @Test
    void shouldDeleteHospitalDoctor() throws Exception {
        HospitalDoctor deletedHospitalDoctor = HospitalDoctor.builder()
                .deletedAt(Instant.now())
                .build();

        when(hospitalDoctorService.deleteHospitalDoctor(1L,1L)).thenReturn(deletedHospitalDoctor);

        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(delete("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenDeleting() throws Exception {
        mockMvc.perform(delete("/api/hospitals/hospital-doctor/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfHospitalDoctorNotFoundWhenDeleting() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalDoctorService.deleteHospitalDoctor(1L, 1L))
                .thenThrow(new ResourceNotFoundException("Cannot delete. No active hospitalDoctor found with ID: "+1L));

        mockMvc.perform(delete("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeletingHospitalDoctor() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(hospitalDoctorService.deleteHospitalDoctor(1L,1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(delete("/api/hospitals/hospital-doctor/1")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleHospitalDoctor)))
                .andExpect(status().isInternalServerError());
    }
}
