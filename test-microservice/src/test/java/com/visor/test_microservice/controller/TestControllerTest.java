package com.visor.test_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.test_microservice.client.DoctorClient;
import com.visor.test_microservice.client.HospitalClient;
import com.visor.test_microservice.client.PatientClient;
import com.visor.test_microservice.dto.PatientTestDTO;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.TestRepository;
import com.visor.test_microservice.service.TestService;
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

@WebMvcTest(controllers = TestController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class TestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestService testService;

    @MockitoBean
    private TestRepository testRepository;

    @MockitoBean
    private DoctorClient doctorClient;

    @MockitoBean
    private PatientClient patientClient;

    @MockitoBean
    private HospitalClient hospitalClient;

    @Autowired
    private ObjectMapper objectMapper;

    private TestEntity sampleTestEntity;

    @BeforeEach
    void setUp() {
        sampleTestEntity = TestEntity.builder()
                .id("id")
                .doctorId(1L)
                .patientId(2L)
                .hospitalId(3L)
                .createdAt(Instant.now())
                .build();
    }

    // --- POST /api/tests ---

    @Test
    void shouldCreateTest() throws Exception {
        when(testService.createTestEntity(any(TestEntity.class))).thenReturn(sampleTestEntity);
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(post("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTestEntity))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(1L))
                .andExpect(jsonPath("$.patientId").value(2L))
                .andExpect(jsonPath("$.hospitalId").value(3L));
    }

    @Test
    void shouldReturn400IfInvalidTestData() throws Exception {
        TestEntity sampleInvalidTestEntity = TestEntity.builder()
                .build();

        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(post("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleInvalidTestEntity))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenCreating() throws Exception {
        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTestEntity)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404IfHospitalOrPatientNotFoundWhenCreating() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.createTestEntity(any(TestEntity.class)))
                .thenThrow(new ResourceNotFoundException("Hospital or Patient not found"));

        mockMvc.perform(post("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTestEntity))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCreatingTest() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.createTestEntity(any(TestEntity.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests ---

    @Test
    void shouldGetAllTest() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getAllTests()).thenReturn(List.of(sampleTestEntity));

        mockMvc.perform(get("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value("1"));
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingAllTest() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getAllTests()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/tests")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests/{id} ---

    @Test
    void shouldGetTestById() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getTestById("id")).thenReturn(sampleTestEntity);

        mockMvc.perform(get("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value("1"));
    }

    @Test
    void shouldReturn404IfTestNotFoundById() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getTestById("id")).thenThrow(new ResourceNotFoundException("Test not found"));

        mockMvc.perform(get("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingTestById() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getTestById("id")).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests/passcode/{passcode} ---
    @Test
    void shouldGetTestByPasscode() throws Exception {
        PatientTestDTO sampleTestDTO = PatientTestDTO.builder()
                .id("id")
                .doctorId(1L)
                .patientId(2L)
                .hospitalId(3L)
                .createdAt(Instant.now())
                .passCode("passcode")
                .build();

        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getPatientTestByPasscode("passcode")).thenReturn(sampleTestDTO);

        mockMvc.perform(get("/api/tests/passcode/passcode")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value("1"));
    }

    @Test
    void shouldReturn404IfTestNotFoundByPasscode() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getPatientTestByPasscode("passcode")).thenThrow(new ResourceNotFoundException("Test not found"));

        mockMvc.perform(get("/api/tests/passcode/passcode")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingTestByPasscode() throws Exception {
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.getPatientTestByPasscode("passcode")).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/tests/passcode/passcode")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/tests/{Id} ---

    @Test
    void shouldUpdateTest() throws Exception {
        TestEntity updatedTestEntity = TestEntity.builder()
                .id("id")
                .doctorId(1L)
                .patientId(3L)
                .hospitalId(4L)
                .build();

        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.updateTestEntity(eq("id"), any(TestEntity.class))).thenReturn(updatedTestEntity);

        mockMvc.perform(put("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTestEntity))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(3L))
                .andExpect(jsonPath("$.hospitalId").value(4L));
    }

    @Test
    void shouldReturn400IfInvalidTestDataWhenUpdating() throws Exception {
        TestEntity sampleInvalidTestEntity = TestEntity.builder()
                .build();

        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        mockMvc.perform(put("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleInvalidTestEntity))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfInvalidTestDataWhenUpdating() throws Exception {
        TestEntity sampleInvalidTestEntity = TestEntity.builder()
                .id("id")
                .doctorId(1L)
                .patientId(3L)
                .hospitalId(4L)
                .build();

        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(testService.updateTestEntity(eq("id"), any(TestEntity.class)))
                .thenThrow(new ResourceNotFoundException("Hospital or Patient not found"));

        mockMvc.perform(put("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleInvalidTestEntity))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenUpdatingTest() throws Exception {
        when(testService.updateTestEntity(eq("id"), any(TestEntity.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(doctorClient.getDoctorByKeycloakId("keycloak-id-123")).thenReturn(1L);

        mockMvc.perform(put("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTestEntity))
                )
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/tests/{Id} ---

    @Test
    void shouldDeleteTest() throws Exception {
        mockMvc.perform(delete("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404IfTestNotFoundWhenDeleting() throws Exception {
        doThrow(new ResourceNotFoundException("Test not found"))
                .when(testService).deleteTestEntity(eq("id"));

        mockMvc.perform(delete("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeletingTest() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(testService).deleteTestEntity(eq("id"));

        mockMvc.perform(delete("/api/tests/id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }
}
