package com.visor.test_microservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Document(collection = "tests")
@Getter
@Setter
@NoArgsConstructor
public class TestEntity {

    @Id
    private String id;

    private Instant createdAt;

    private Instant deletedAt;

    private String passCode;

    private Set<FileAttachment> attachments = new HashSet<>();

    private Set<ImageStack> imageStacks = new HashSet<>();

    @PrePersist
    public void generatePassCode() {
        this.passCode = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    }
}
