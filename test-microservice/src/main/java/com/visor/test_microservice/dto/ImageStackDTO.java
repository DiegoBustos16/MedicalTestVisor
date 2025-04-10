package com.visor.test_microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ImageStackDTO {
    @Schema(description = "Image stack unique identifier", example = "stk-001")
    private String id;

    @Schema(description = "Name of the image stack", example = "Femur Joint")
    private String stackName;

    @Schema(description = "Timestamp when the image stack was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the image stack was deleted, if applicable")
    private Instant deletedAt;

    @Schema(description = "Identifier of the test to which this stack belongs", example = "tst-001")
    private String testId;

    @Schema(description = "List of image files in this stack")
    private List<ImageFileDTO> imageFiles;
}
