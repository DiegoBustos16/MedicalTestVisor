package com.visor.doctor_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.repository.DoctorRepository;
import com.visor.doctor_microservice.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    void shouldDeleteDoctor() throws Exception {
        when(doctorService.getDoctorIdByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(delete("/api/doctors")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNoContent());
    }
}
