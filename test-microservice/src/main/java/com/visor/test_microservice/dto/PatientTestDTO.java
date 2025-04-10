package com.visor.test_microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PatientTestDTO {
    @Schema(description = "Test unique identifier", example = "tst-001")
    private String id;

    @Schema(description = "Doctor ID assigned to the test", example = "1")
    private Long doctorId;

    @Schema(description = "Patient ID for the test", example = "2")
    private Long patientId;

    @Schema(description = "Hospital ID for the test", example = "5")
    private Long hospitalId;

    @Schema(description = "Timestamp when the test was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the test was deleted, if applicable")
    private Instant deletedAt;

    @Schema(description = "Passcode to retrieve the test", example = "ABC123")
    private String passCode;

    @Schema(description = "List of image stacks associated with this test")
    private List<ImageStackDTO> imageStacks;

    @Schema(description = "List of file attachments associated with this test (if any)")
    private List<FileAttachmentDTO> fileAttachments;
}

