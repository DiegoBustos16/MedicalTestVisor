package com.visor.test_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.exception.InvalidFileException;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageFileRepository;
import com.visor.test_microservice.service.ImageFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

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

@WebMvcTest(controllers = ImageFileController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class ImageFileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageFileService imageFileService;

    @MockitoBean
    private ImageFileRepository imageFileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ImageFile sampleImageFile;

    @BeforeEach
    void setUp() {
        sampleImageFile = ImageFile.builder()
                .id("sample-id")
                .fileUrl("http://example.com/sample.jpg")
                .imageStackId("sample-stack-id")
                .build();
    }

    // --- POST /api/tests/image-files ---

    @Test
    void shouldCreateImageFile() throws Exception {
        when(imageFileService.saveImageFile(any(MultipartFile.class), eq("sample-stack-id")))
                .thenReturn(sampleImageFile);

        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "sample data".getBytes());

        mockMvc.perform(multipart("/api/tests/image-files")
                        .file(file)
                        .param("imageStackId", "sample-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sample-id"))
                .andExpect(jsonPath("$.fileUrl").value("http://example.com/sample.jpg"))
                .andExpect(jsonPath("$.imageStackId").value("sample-stack-id"));
    }

    @Test
    void shouldReturn400ForInvalidImageFileDataWhenCreatingImageFile() throws Exception {
        when(imageFileService.saveImageFile(any(MultipartFile.class), eq("sample-stack-id")))
                .thenThrow(new InvalidFileException("The file is empty or null"));

        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "sample data".getBytes());

        mockMvc.perform(multipart("/api/tests/image-files")
                        .file(file)
                        .param("imageStackId", "sample-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentImageStackIdWhenCreatingImageFile() throws Exception {
        when(imageFileService.saveImageFile(any(MultipartFile.class), eq("missing-stack-id")))
                .thenThrow(new ResourceNotFoundException("No active Image Stack found with ID: missing-stack-id"));

        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "sample data".getBytes());

        mockMvc.perform(multipart("/api/tests/image-files")
                        .file(file)
                        .param("imageStackId", "missing-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenCreatingImageFile() throws Exception {
        when(imageFileService.saveImageFile(any(MultipartFile.class), eq("sample-stack-id")))
                .thenThrow(new RuntimeException("Internal Server Error"));

        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "sample data".getBytes());

        mockMvc.perform(multipart("/api/tests/image-files")
                        .file(file)
                        .param("imageStackId", "sample-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests/image-files/stack/{imageStackId} ---

    @Test
    void shouldGetImageFilesByImageStackId() throws Exception {
        when(imageFileService.getImageFilesByImageStackId("sample-stack-id"))
                .thenReturn(List.of(sampleImageFile));

        mockMvc.perform(get("/api/tests/image-files/stack/sample-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sample-id"))
                .andExpect(jsonPath("$[0].fileUrl").value("http://example.com/sample.jpg"))
                .andExpect(jsonPath("$[0].imageStackId").value("sample-stack-id"));
    }

    @Test
    void shouldReturn404ForNonExistentImageStackIdWhenGettingImageFiles() throws Exception {
        when(imageFileService.getImageFilesByImageStackId("missing-stack-id"))
                .thenThrow(new ResourceNotFoundException("No active Image Stack found with ID: missing-stack-id"));

        mockMvc.perform(get("/api/tests/image-files/stack/missing-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenGettingImageFiles() throws Exception {
        when(imageFileService.getImageFilesByImageStackId("sample-stack-id"))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/tests/image-files/stack/sample-stack-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/tests/image-files/{id} ---

    @Test
    void shouldDeleteImageFile() throws Exception {
        mockMvc.perform(delete("/api/tests/image-files/sample-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForNonExistentImageFileIdWhenDeleting() throws Exception {
        doThrow(new ResourceNotFoundException("No active image file found with ID: missing-imagefile-id"))
                .when(imageFileService).deleteImageFile(eq("missing-imagefile-id"));

        mockMvc.perform(delete("/api/tests/image-files/missing-imagefile-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500IfUnexpectedErrorWhenDeletingImageFile() throws Exception {
        doThrow(new RuntimeException("Internal Server Error"))
                .when(imageFileService).deleteImageFile(eq("sample-id"));
        mockMvc.perform(delete("/api/tests/image-files/sample-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }
}
