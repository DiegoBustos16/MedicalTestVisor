package com.visor.test_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "image_files")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageFile {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @CreatedDate
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String fileUrl;

    @Schema(example = "67cb1a468fd12818a2a57235")
    @NotNull(message = "Stack must not be null")
    private String imageStackId;
}
