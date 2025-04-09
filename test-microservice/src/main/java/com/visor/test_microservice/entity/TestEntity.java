package com.visor.test_microservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;
@Document(collection = "tests")
@Getter
@Setter
@NoArgsConstructor
public class TestEntity {

    @Id
    private String id;

    @NotNull(message = "Doctor must not be null")
    private Long doctorId;

    @NotNull(message = "Patient must not be null")
    private Long patientId;

    @NotNull(message = "Hospital must not be null")
    private Long hospitalId;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    private String passCode;

    private Set<FileAttachment> attachments = new HashSet<>();

    private Set<ImageStack> imageStacks = new HashSet<>();
}
