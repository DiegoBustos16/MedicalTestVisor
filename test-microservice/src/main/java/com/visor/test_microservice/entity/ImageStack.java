package com.visor.test_microservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
    private Long id;

    private Instant createdAt;

    private Instant deletedAt;

    private String stackName;

    private Set<ImageFile> images = new HashSet<>();

    @DBRef
    private TestEntity testEntity;
}
