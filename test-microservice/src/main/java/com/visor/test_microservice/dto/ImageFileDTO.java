package com.visor.test_microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class ImageFileDTO {
    @Schema(description = "Image file unique identifier", example = "img-001")
    private String id;

    @Schema(description = "URL of the image file", example = "https://s3.amazonaws.com/bucket/image1.jpg")
    private String fileUrl;

    @Schema(description = "Timestamp when the image file was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the image file was deleted, if applicable")
    private Instant deletedAt;

    @Schema(description = "Identifier of the image stack this file belongs to", example = "stk-001")
    private String imageStackId;
}

