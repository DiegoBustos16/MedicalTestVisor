package com.visor.test_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
@Document(collection = "image_stacks")
@Getter
@Setter
@NoArgsConstructor
public class ImageStack {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @CreatedDate
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @Schema(example = "Femur Joint")
    private String stackName;

    @Schema(example = "67cb1a468fd12818a2a57235")
    @NotNull(message = "Test must not be null")
    private String testId;
}
