package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.repository.HospitalRepository;
import com.visor.hospital_microservice.service.DoctorService;
import com.visor.hospital_microservice.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class DoctorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalService hospitalService;

    @MockitoBean
    private HospitalRepository hospitalRepository;

    @MockitoBean
    private DoctorService doctorService;

    private DoctorDTO sampleDoctor;

    @BeforeEach
    void setUp() {
        sampleDoctor = DoctorDTO.builder()
                .id(1L)
                .firstName("Diego")
                .lastName("Bustos")
                .email("diegombustos16@gmail.com")
                .licenseNumber("ABC123")
                .build();
    }

    // --- GET /api/hospitals/doctors/hospital ---

    @Test
    void shouldGetAllDoctorsOfHospital() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(doctorService.getAllDoctorsByHospital(1L)).thenReturn(List.of(sampleDoctor));

        mockMvc.perform(get("/api/hospitals/doctors/hospital")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Diego"));
    }

    @Test
    void shouldReturn403IfNoJwtAuthenticationWhenGetting() throws Exception {
        mockMvc.perform(delete("/api/hospitals/doctors/hospital"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenUpdatingDoctor() throws Exception {
        when(hospitalService.getHospitalIdByKeycloakId("keycloak-id-123")).thenReturn(1L);
        when(doctorService.getAllDoctorsByHospital(1L)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(patch("/api/hospitals/doctors/hospital")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("HOSPITAL")));
                        })))
                .andExpect(status().isInternalServerError());
    }
}
