package com.visor.test_microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class FileAttachmentDTO {
    @Schema(description = "File attachment unique identifier", example = "att-001")
    private String id;

    @Schema(description = "Name of the file attachment", example = "Doctor Report")
    private String fileName;

    @Schema(description = "URL of the file attachment", example = "https://s3.amazonaws.com/bucket/report.pdf")
    private String fileUrl;

    @Schema(description = "Timestamp when the file attachment was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the file attachment was deleted, if applicable")
    private Instant deletedAt;

    @Schema(description = "Test identifier to which this attachment belongs", example = "tst-001")
    private String testId;
}

