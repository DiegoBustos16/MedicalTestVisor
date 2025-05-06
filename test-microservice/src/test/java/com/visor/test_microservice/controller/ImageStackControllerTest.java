package com.visor.test_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageStackRepository;
import com.visor.test_microservice.service.ImageStackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(controllers = ImageStackController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class ImageStackControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageStackService imageStackService;

    @MockitoBean
    private ImageStackRepository imageStackRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ImageStack sampleImageStack;

    @BeforeEach
    void setUp() {
        sampleImageStack = ImageStack.builder()
                .id("sample-id")
                .stackName("Sample Stack")
                .testId("sample-test-id")
                .build();
    }

    // --- POST /api/tests/image-stacks ---

    @Test
    void shouldCreateImageStack() throws Exception {
        when(imageStackService.createImageStack(any(ImageStack.class))).thenReturn(sampleImageStack);

        mockMvc.perform(post("/api/tests/image-stacks")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleImageStack))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sample-id"))
                .andExpect(jsonPath("$.stackName").value("Sample Stack"))
                .andExpect(jsonPath("$.testId").value("sample-test-id"));
    }

    @Test
    void shouldReturn400ForInvalidImageStackDataWhenCreatingImageStack() throws Exception {
        ImageStack invalidImageStack = new ImageStack();

        mockMvc.perform(post("/api/tests/image-stacks")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidImageStack))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentTestIdWhenCreatingImageStack() throws Exception {
        when(imageStackService.createImageStack(any(ImageStack.class))).thenThrow(new ResourceNotFoundException("No active test found with ID: "));

        mockMvc.perform(post("/api/tests/image-stacks")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleImageStack))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCreatingImageStack() throws Exception {
        when(imageStackService.createImageStack(any(ImageStack.class))).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/api/tests/image-stacks")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleImageStack))
                )
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests/image-stacks/test/{testEntityId} ---

    @Test
    void shouldGetImageStacksByTestEntityId() throws Exception {
        when(imageStackService.getImageStacksByTestEntityId("sample-test-id")).thenReturn(List.of(sampleImageStack));

        mockMvc.perform(get("/api/tests/image-stacks/test/sample-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sample-id"))
                .andExpect(jsonPath("$[0].stackName").value("Sample Stack"))
                .andExpect(jsonPath("$[0].testId").value("sample-test-id"));
    }

    @Test
    void shouldReturn404IfTestNotFoundById() throws Exception {
        when(imageStackService.getImageStacksByTestEntityId("non-existent-test-id")).thenThrow(new ResourceNotFoundException("No active test found with ID: non-existent-test-id"));

        mockMvc.perform(get("/api/tests/image-stacks/test/non-existent-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingImageStacks() throws Exception {
        when(imageStackService.getImageStacksByTestEntityId("sample-test-id")).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/tests/image-stacks/test/sample-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/tests/image-stacks/{id} ---

    @Test
    void shouldDeleteImageStack() throws Exception {
        mockMvc.perform(delete("/api/tests/image-stacks/sample-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404IfImageStackNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("No active image stack found with ID: non-existent-id"))
                .when(imageStackService).deleteImageStack(eq("non-existent-id"));

        mockMvc.perform(delete("/api/tests/image-stacks/non-existent-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeletingImageStack() throws Exception {
        doThrow(new RuntimeException("Internal Server Error"))
                .when(imageStackService).deleteImageStack(eq("sample-id"));

        mockMvc.perform(delete("/api/tests/image-stacks/sample-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        }))
                )
                .andExpect(status().isInternalServerError());
    }

}
