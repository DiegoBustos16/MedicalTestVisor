package com.visor.test_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "file_attachments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachment {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @CreatedDate
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @Schema(example = "Doctor Report")
    @NotNull(message = "File name must not be null")
    private String fileName;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String fileUrl;

    @Schema(example = "67cb1a468fd12818a2a57235")
    @NotNull(message = "Test must not be null")
    private String testId;
}

