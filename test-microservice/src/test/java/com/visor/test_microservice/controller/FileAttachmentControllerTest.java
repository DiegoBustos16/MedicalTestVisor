package com.visor.test_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.exception.InvalidFileException;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import com.visor.test_microservice.service.FileAttachmentService;
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

@WebMvcTest(controllers = FileAttachmentController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class FileAttachmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileAttachmentService fileAttachmentService;

    @MockitoBean
    private FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private FileAttachment sampleFileAttachment;

    @BeforeEach
    void setUp() {
        sampleFileAttachment = FileAttachment.builder()
                .id("sample-id")
                .fileUrl("http://example.com/sample.pdf")
                .testId("sample-test-id")
                .build();
    }

    // --- POST /api/tests/file-attachments ---

    @Test
    void shouldCreateFileAttachment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[1]);
        String testId = "valid-test-id";

        when(fileAttachmentService.saveFileAttachment(file, testId)).thenReturn(sampleFileAttachment);

        mockMvc.perform(multipart("/api/tests/file-attachments")
                        .file(file)
                        .param("testId", testId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleFileAttachment.getId()))
                .andExpect(jsonPath("$.fileUrl").value(sampleFileAttachment.getFileUrl()))
                .andExpect(jsonPath("$.testId").value(sampleFileAttachment.getTestId()));
    }

    @Test
    void shouldReturn400ForInvalidFileDataWhenCreatingFileAttachment() throws Exception {
        when(fileAttachmentService.saveFileAttachment(any(MultipartFile.class), eq("valid-test-id")))
                .thenThrow(new InvalidFileException("The file is empty or null"));

        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[1]);

        mockMvc.perform(multipart("/api/tests/file-attachments")
                        .file(file)
                        .param("testId", "valid-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentTestIdWhenCreatingFileAttachment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[1]);

        when(fileAttachmentService.saveFileAttachment(file, "missing-test-id"))
                .thenThrow(new ResourceNotFoundException("No active Test found with ID: missing-test-id"));

        mockMvc.perform(multipart("/api/tests/file-attachments")
                        .file(file)
                        .param("testId", "missing-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500ForErrorUploadingFileToS3WhenCreatingFileAttachment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[1]);

        when(fileAttachmentService.saveFileAttachment(file, "valid-test-id"))
                .thenThrow(new RuntimeException("Error uploading file to S3"));

        mockMvc.perform(multipart("/api/tests/file-attachments")
                        .file(file)
                        .param("testId", "valid-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturn500ForInternalServerErrorWhenCreatingFileAttachment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[1]);

        when(fileAttachmentService.saveFileAttachment(file, "valid-test-id"))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(multipart("/api/tests/file-attachments")
                        .file(file)
                        .param("testId", "valid-test-id")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/tests/file-attachments/test/{testEntityId} ---

    @Test
    void shouldGetFileAttachmentsByTestEntityId() throws Exception {
        String testEntityId = "valid-test-id";
        when(fileAttachmentService.getAttachmentsByTestEntityId(testEntityId))
                .thenReturn(List.of(sampleFileAttachment));

        mockMvc.perform(get("/api/tests/file-attachments/test/{testEntityId}", testEntityId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleFileAttachment.getId()))
                .andExpect(jsonPath("$[0].fileUrl").value(sampleFileAttachment.getFileUrl()))
                .andExpect(jsonPath("$[0].testId").value(sampleFileAttachment.getTestId()));
    }

    @Test
    void shouldReturn404ForNonExistentTestEntityIdWhenGettingFileAttachments() throws Exception {
        String testEntityId = "missing-test-id";

        when(fileAttachmentService.getAttachmentsByTestEntityId(testEntityId))
                .thenThrow(new ResourceNotFoundException("No active Test found with ID: " + testEntityId));

        mockMvc.perform(get("/api/tests/file-attachments/test/{testEntityId}", testEntityId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500ForInternalServerErrorWhenGettingFileAttachments() throws Exception {
        String testEntityId = "valid-test-id";

        when(fileAttachmentService.getAttachmentsByTestEntityId(testEntityId))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/tests/file-attachments/test/{testEntityId}", testEntityId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/tests/file-attachments/{id} ---
    @Test
    void shouldDeleteFileAttachment() throws Exception {
        String attachmentId = "valid-attachment-id";

        mockMvc.perform(delete("/api/tests/file-attachments/{id}", attachmentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForNonExistentFileAttachmentWhenDeleting() throws Exception {
        String attachmentId = "missing-attachment-id";
        doThrow(new ResourceNotFoundException("No active attachment found with ID: " + attachmentId))
                .when(fileAttachmentService).deleteFileAttachment(attachmentId);

        mockMvc.perform(delete("/api/tests/file-attachments/{id}", attachmentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500ForInternalServerErrorWhenDeletingFileAttachment() throws Exception {
        String attachmentId = "valid-attachment-id";

        doThrow(new RuntimeException("Internal Server Error"))
                .when(fileAttachmentService).deleteFileAttachment(attachmentId);

        mockMvc.perform(delete("/api/tests/file-attachments/{id}", attachmentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", "keycloak-id-123");
                            jwt.claim("realm_access", Map.of("roles", List.of("DOCTOR")));
                        })))
                .andExpect(status().isInternalServerError());
    }
}
