package com.visor.test_microservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotNull;
@Document(collection = "tests")
@Getter
@Setter
@NoArgsConstructor
public class TestEntity {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "Doctor must not be null")
    private Long doctorId;

    @Schema(example = "2")
    @NotNull(message = "Patient must not be null")
    private Long patientId;

    @Schema(example = "5")
    @NotNull(message = "Hospital must not be null")
    private Long hospitalId;

    @CreatedDate
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant deletedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String passCode;
}
