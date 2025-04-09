package com.visor.test_microservice.entity;

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
    private String id;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    private String stackName;

    private Set<ImageFile> images = new HashSet<>();

    @NotNull(message = "Test must not be null")
    private String testId;
}
