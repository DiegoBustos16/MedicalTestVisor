package com.visor.test_microservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "image_files")
@Getter
@Setter
@NoArgsConstructor
public class ImageFile {
    @Id
    private Long id;

    private Instant createdAt;

    private Instant deletedAt;

    private byte[] imageData;

    @DBRef
    private ImageStack imageStack;
}
